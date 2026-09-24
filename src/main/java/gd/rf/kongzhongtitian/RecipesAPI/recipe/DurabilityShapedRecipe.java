package gd.rf.kongzhongtitian.RecipesAPI.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DurabilityShapedRecipe implements CraftingRecipe {
    private final ResourceLocation id;
    private final String group;
    private final CraftingBookCategory category;
    private final int width;
    private final int height;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;

    public DurabilityShapedRecipe(
            ResourceLocation id, String group, CraftingBookCategory category,
            int width, int height,
            NonNullList<Ingredient> ingredients, ItemStack result
    ) {
        this.id = id;
        this.group = group;
        this.category = category;
        this.width = width;
        this.height = height;
        this.ingredients = ingredients;
        this.result = result;
    }

    @Override
    public boolean matches(CraftingContainer container, @NotNull Level level) {
        int cWidth = container.getWidth();
        int cHeight = container.getHeight();
        if (cWidth >= this.width && cHeight >= this.height) {
            for (int xOff = 0; xOff <= cWidth - this.width; xOff++) {
                for (int yOff = 0; yOff <= cHeight - this.height; yOff++) {
                    if (this.checkMatch(container, xOff, yOff, false)) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private boolean checkMatch(CraftingContainer container, int xOff, int yOff, boolean mirrored) {
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                int slotX = x + xOff;
                int slotY = y + yOff;
                int slotIndex = slotY * container.getWidth() + slotX;
                ItemStack stack = container.getItem(slotIndex);
                Ingredient ingredient = this.ingredients.get(y * this.width + x);
                if (!ingredient.test(stack)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= this.width && height >= this.height;
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
        return ModRecipes.DURABILITY_SHAPED_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    public static class Serializer implements RecipeSerializer<DurabilityShapedRecipe> {

        @Override
        public DurabilityShapedRecipe fromJson(ResourceLocation id, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            CraftingBookCategory category = CraftingBookCategory.CODEC.byName(
                    GsonHelper.getAsString(json, "category", null),
                    CraftingBookCategory.MISC
            );
            Map<String, Ingredient> keyMap = readKey(
                    GsonHelper.getAsJsonObject(json, "key"));
            String[] pattern = readPattern(
                    GsonHelper.getAsJsonArray(json, "pattern"));
            int width = pattern[0].length();
            int height = pattern.length;
            NonNullList<Ingredient> ingredients =
                    dissolvePattern(pattern, keyMap, width, height);
            ItemStack result = ShapedRecipe.itemStackFromJson(
                    GsonHelper.getAsJsonObject(json, "result"));
            return new DurabilityShapedRecipe(
                    id, group, category, width, height, ingredients, result);
        }

        private static Map<String, Ingredient> readKey(JsonObject keyObj) {
            Map<String, Ingredient> map = new HashMap<>();

            for (Entry<String, JsonElement> entry : keyObj.entrySet()) {
                if (entry.getKey().length() != 1) {
                    throw new JsonSyntaxException(
                            "Invalid key entry: '" + entry.getKey()
                                    + "' is not a one character string");
                }
                if (" ".equals(entry.getKey())) {
                    throw new JsonSyntaxException(
                            "Invalid key entry: ' ' is a reserved character");
                }
                map.put(entry.getKey(),
                        Ingredient.fromJson(entry.getValue(), false));
            }

            map.put(" ", Ingredient.EMPTY);
            return map;
        }

        private static String[] readPattern(JsonArray patternArr) {
            String[] pattern = new String[patternArr.size()];

            for (int i = 0; i < patternArr.size(); i++) {
                String line = GsonHelper.convertToString(
                        patternArr.get(i), "pattern[" + i + "]");
                if (i > 0 && pattern[0].length() != line.length()) {
                    throw new JsonSyntaxException(
                            "Invalid pattern: each row must be the same width");
                }
                pattern[i] = line;
            }

            return pattern;
        }

        private static NonNullList<Ingredient> dissolvePattern(
                String[] pattern, Map<String, Ingredient> keyMap,
                int width, int height) {
            NonNullList<Ingredient> ingredients =
                    NonNullList.withSize(width * height, Ingredient.EMPTY);

            for (int y = 0; y < height; y++) {
                String row = pattern[y];

                for (int x = 0; x < width; x++) {
                    char c = row.charAt(x);
                    Ingredient ing = keyMap.get(String.valueOf(c));
                    if (ing == null) {
                        throw new JsonSyntaxException(
                                "Pattern references symbol '" + c
                                        + "' but it's not defined in the key");
                    }
                    ingredients.set(y * width + x, ing);
                }
            }

            return ingredients;
        }

        @Override
        @Nullable
        public DurabilityShapedRecipe fromNetwork(
                ResourceLocation id, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            CraftingBookCategory category =
                    buffer.readEnum(CraftingBookCategory.class);
            int width = buffer.readVarInt();
            int height = buffer.readVarInt();
            NonNullList<Ingredient> ingredients =
                    NonNullList.withSize(width * height, Ingredient.EMPTY);

            for (int i = 0; i < ingredients.size(); i++) {
                ingredients.set(i, Ingredient.fromNetwork(buffer));
            }

            ItemStack result = buffer.readItem();
            return new DurabilityShapedRecipe(
                    id, group, category, width, height, ingredients, result);
        }

        @Override
        public void toNetwork(
                FriendlyByteBuf buffer, DurabilityShapedRecipe recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeEnum(recipe.category);
            buffer.writeVarInt(recipe.width);
            buffer.writeVarInt(recipe.height);

            for (Ingredient ingredient : recipe.ingredients) {
                ingredient.toNetwork(buffer);
            }

            buffer.writeItem(recipe.result);
        }
    }
}