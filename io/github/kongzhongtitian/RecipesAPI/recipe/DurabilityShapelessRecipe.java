package io.github.kongzhongtitian.RecipesAPI.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
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

   public DurabilityShapelessRecipe(ResourceLocation id, String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients) {
      this.id = id;
      this.group = group;
      this.category = category;
      this.result = result;
      this.ingredients = ingredients;
      this.isSimple = ingredients.stream().allMatch(Ingredient::isSimple);
   }

   public boolean matches(CraftingContainer container, @NotNull Level level) {
      List<ItemStack> inputs = new ArrayList();
      int emptyCount = 0;

      for(int i = 0; i < container.m_6643_(); ++i) {
         ItemStack stack = container.m_8020_(i);
         if (!stack.m_41619_()) {
            inputs.add(stack);
         } else {
            ++emptyCount;
         }
      }

      if (inputs.size() == this.ingredients.size() && emptyCount != container.m_6643_()) {
         return RecipeMatcher.findMatches(inputs, this.ingredients) != null;
      } else {
         return false;
      }
   }

   public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
      return this.result.m_41777_();
   }

   public boolean m_8004_(int width, int height) {
      return width * height >= this.ingredients.size();
   }

   public ItemStack m_8043_(RegistryAccess access) {
      return this.result;
   }

   @NotNull
   public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
      NonNullList<ItemStack> remaining = NonNullList.m_122780_(container.m_6643_(), ItemStack.f_41583_);

      for(int i = 0; i < remaining.size(); ++i) {
         ItemStack stack = container.m_8020_(i);
         if (stack.m_41720_().m_41465_() && stack.m_41763_()) {
            ItemStack damaged = stack.m_41777_();
            damaged.m_41721_(damaged.m_41773_() + 1);
            if (damaged.m_41773_() >= damaged.m_41776_()) {
               remaining.set(i, ItemStack.f_41583_);
            } else {
               remaining.set(i, damaged);
            }
         } else if (stack.m_41720_().hasCraftingRemainingItem(stack)) {
            remaining.set(i, stack.m_41720_().getCraftingRemainingItem(stack));
         }
      }

      return remaining;
   }

   public NonNullList<Ingredient> m_7527_() {
      return this.ingredients;
   }

   public boolean m_5598_() {
      return true;
   }

   public String m_6076_() {
      return this.group;
   }

   public ItemStack m_8042_() {
      return new ItemStack(Items.f_41960_);
   }

   public ResourceLocation m_6423_() {
      return this.id;
   }

   public RecipeSerializer<?> m_7707_() {
      return (RecipeSerializer)ModRecipes.DURABILITY_SHAPELESS_SERIALIZER.get();
   }

   public RecipeType<?> m_6671_() {
      return RecipeType.f_44107_;
   }

   public CraftingBookCategory m_245232_() {
      return this.category;
   }

   // $FF: synthetic method
   // $FF: bridge method
   @NotNull
   public NonNullList m_7457_(Container var1) {
      return this.getRemainingItems((CraftingContainer)var1);
   }

   // $FF: synthetic method
   // $FF: bridge method
   public ItemStack m_5874_(Container var1, RegistryAccess var2) {
      return this.assemble((CraftingContainer)var1, var2);
   }

   // $FF: synthetic method
   // $FF: bridge method
   public boolean m_5818_(Container var1, @NotNull Level var2) {
      return this.matches((CraftingContainer)var1, var2);
   }

   public static class Serializer implements RecipeSerializer<DurabilityShapelessRecipe> {
      public DurabilityShapelessRecipe fromJson(ResourceLocation id, JsonObject json) {
         String group = GsonHelper.m_13851_(json, "group", "");
         CraftingBookCategory category = (CraftingBookCategory)CraftingBookCategory.f_244644_.m_262792_(GsonHelper.m_13851_(json, "category", (String)null), CraftingBookCategory.MISC);
         NonNullList<Ingredient> ingredients = this.readIngredients(GsonHelper.m_13933_(json, "ingredients"));
         if (ingredients.isEmpty()) {
            throw new JsonParseException("No ingredients for durability shapeless recipe");
         } else {
            ItemStack result = ShapedRecipe.m_151274_(GsonHelper.m_13930_(json, "result"));
            return new DurabilityShapelessRecipe(id, group, category, result, ingredients);
         }
      }

      private NonNullList<Ingredient> readIngredients(JsonArray array) {
         NonNullList<Ingredient> list = NonNullList.m_122779_();

         for(int i = 0; i < array.size(); ++i) {
            Ingredient ingredient = Ingredient.m_288218_(array.get(i), false);
            if (!ingredient.m_43947_()) {
               list.add(ingredient);
            }
         }

         return list;
      }

      @Nullable
      public DurabilityShapelessRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
         String group = buffer.m_130277_();
         CraftingBookCategory category = (CraftingBookCategory)buffer.m_130066_(CraftingBookCategory.class);
         int size = buffer.m_130242_();
         NonNullList<Ingredient> ingredients = NonNullList.m_122780_(size, Ingredient.f_43901_);
         ingredients.replaceAll((ignored) -> {
            return Ingredient.m_43940_(buffer);
         });
         ItemStack result = buffer.m_130267_();
         return new DurabilityShapelessRecipe(id, group, category, result, ingredients);
      }

      public void toNetwork(FriendlyByteBuf buffer, DurabilityShapelessRecipe recipe) {
         buffer.m_130070_(recipe.group);
         buffer.m_130068_(recipe.category);
         buffer.m_130130_(recipe.ingredients.size());
         Iterator var3 = recipe.ingredients.iterator();

         while(var3.hasNext()) {
            Ingredient ingredient = (Ingredient)var3.next();
            ingredient.m_43923_(buffer);
         }

         buffer.m_130055_(recipe.result);
      }

      // $FF: synthetic method
      // $FF: bridge method
      public void m_6178_(FriendlyByteBuf var1, Recipe var2) {
         this.toNetwork(var1, (DurabilityShapelessRecipe)var2);
      }

      // $FF: synthetic method
      // $FF: bridge method
      @Nullable
      public Recipe m_8005_(ResourceLocation var1, FriendlyByteBuf var2) {
         return this.fromNetwork(var1, var2);
      }

      // $FF: synthetic method
      // $FF: bridge method
      public Recipe m_6729_(ResourceLocation var1, JsonObject var2) {
         return this.fromJson(var1, var2);
      }
   }
}
