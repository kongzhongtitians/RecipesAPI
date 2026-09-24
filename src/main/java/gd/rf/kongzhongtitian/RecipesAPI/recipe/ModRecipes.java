package gd.rf.kongzhongtitian.RecipesAPI.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, "recipesapi");
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "recipesapi");
    public static final RegistryObject<RecipeType<DurabilityShapelessRecipe>> DURABILITY_SHAPELESS_TYPE = RECIPE_TYPES.register(
            "durability_shapeless", () -> new RecipeType<DurabilityShapelessRecipe>() {
                public String toString() {
                    return "recipesapi:durability_shapeless";
                }
            }
    );
    public static final RegistryObject<RecipeSerializer<DurabilityShapelessRecipe>> DURABILITY_SHAPELESS_SERIALIZER = RECIPE_SERIALIZERS.register(
            "durability_shapeless", DurabilityShapelessRecipe.Serializer::new
    );
    public static final RegistryObject<RecipeType<DurabilityShapedRecipe>> DURABILITY_SHAPED_TYPE = RECIPE_TYPES.register(
            "durability_shaped", () -> new RecipeType<DurabilityShapedRecipe>() {
                public String toString() {
                    return "recipesapi:durability_shaped";
                }
            }
    );
    public static final RegistryObject<RecipeSerializer<DurabilityShapedRecipe>> DURABILITY_SHAPED_SERIALIZER = RECIPE_SERIALIZERS.register(
            "durability_shaped", DurabilityShapedRecipe.Serializer::new
    );
}
