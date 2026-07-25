package io.github.kongzhongtitian.RecipesAPI.recipe;

import io.github.kongzhongtitian.RecipesAPI.recipe.DurabilityShapelessRecipe.Serializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
   public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES;
   public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS;
   public static final RegistryObject<RecipeType<DurabilityShapelessRecipe>> DURABILITY_SHAPELESS_TYPE;
   public static final RegistryObject<RecipeSerializer<DurabilityShapelessRecipe>> DURABILITY_SHAPELESS_SERIALIZER;

   static {
      RECIPE_TYPES = DeferredRegister.create(Registries.f_256954_, "recipesapi");
      RECIPE_SERIALIZERS = DeferredRegister.create(Registries.f_256764_, "recipesapi");
      DURABILITY_SHAPELESS_TYPE = RECIPE_TYPES.register("durability_shapeless", () -> {
         return new RecipeType<DurabilityShapelessRecipe>() {
            public String toString() {
               return "recipesapi:durability_shapeless";
            }
         };
      });
      DURABILITY_SHAPELESS_SERIALIZER = RECIPE_SERIALIZERS.register("durability_shapeless", Serializer::new);
   }
}
