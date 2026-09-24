package gd.rf.kongzhongtitian.RecipesAPI.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.RecipeMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DurabilityShapelessRecipe implements CraftingRecipe {
    private final ResourceLocation id;
    private final String group;
    private final CraftingBookCategory category;
    private final ItemStack result;
    private final NonNullList<Ingredient> ingredients;
    private final boolean isSimple;

    public DurabilityShapelessRecipe(ResourceLocation id, String group,
                                     CraftingBookCategory category,
                                     ItemStack result,
                                     NonNullList<Ingredient> ingredients) {
        this.id = id;
        this.group = group;
        this.category = category;
        this.result = result;
        this.ingredients = ingredients;
        this.isSimple = ingredients.stream().allMatch(Ingredient::isSimple);
    }

    @Override
    public boolean matches(CraftingContainer container, @NotNull Level level) {
        List<ItemStack> inputs = new ArrayList<>();
        int emptyCount = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                inputs.add(stack);
            } else {
                emptyCount++;
            }
        }

        if (inputs.size() == this.ingredients.size()
                && emptyCount != container.getContainerSize()) {
            return RecipeMatcher.findMatches(inputs, this.ingredients) != null;
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.ingredients.size();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return this.result;
    }

    @Override
    @NotNull
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remaining =
                NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < remaining.size(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem().canBeDepleted()) {
                ItemStack damaged = stack.copy();
                damaged.setDamageValue(damaged.getDamageValue() + 1);
                if (damaged.getDamageValue() >= damaged.getMaxDamage()) {
                    remaining.set(i, ItemStack.EMPTY);
                } else {
                    remaining.set(i, damaged);
                }
            } else if (stack.getItem().hasCraftingRemainingItem(stack)) {
                remaining.set(i, stack.getItem().getCraftingRemainingItem(stack));
            }
        }

        return remaining;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(Items.CRAFTING_TABLE);
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.DURABILITY_SHAPELESS_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    public static class Serializer implements RecipeSerializer<DurabilityShapelessRecipe> {

        @Override
        public DurabilityShapelessRecipe fromJson(ResourceLocation id, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            CraftingBookCategory category = CraftingBookCategory.CODEC.byName(
                    GsonHelper.getAsString(json, "category", null),
                    CraftingBookCategory.MISC
            );
            NonNullList<Ingredient> ingredients =
                    this.readIngredients(GsonHelper.getAsJsonArray(json, "ingredients"));

            if (ingredients.isEmpty()) {
                throw new JsonParseException("No ingredients for durability shapeless recipe");
            }

            ItemStack result = ShapedRecipe.itemStackFromJson(
                    GsonHelper.getAsJsonObject(json, "result"));
            return new DurabilityShapelessRecipe(id, group, category, result, ingredients);
        }

        private NonNullList<Ingredient> readIngredients(JsonArray array) {
            NonNullList<Ingredient> list = NonNullList.create();

            for (int i = 0; i < array.size(); i++) {
                Ingredient ingredient = Ingredient.fromJson(array.get(i), false);
                if (!ingredient.isEmpty()) {
                    list.add(ingredient);
                }
            }

            return list;
        }

        @Override
        @Nullable
        public DurabilityShapelessRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            int size = buffer.readVarInt();
            NonNullList<Ingredient> ingredients =
                    NonNullList.withSize(size, Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buffer));
            ItemStack result = buffer.readItem();
            return new DurabilityShapelessRecipe(id, group, category, result, ingredients);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, DurabilityShapelessRecipe recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeEnum(recipe.category);
            buffer.writeVarInt(recipe.ingredients.size());
            for (Ingredient ingredient : recipe.ingredients) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeItem(recipe.result);
        }
    }
}