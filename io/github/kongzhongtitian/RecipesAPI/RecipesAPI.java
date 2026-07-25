package io.github.kongzhongtitian.RecipesAPI;

import io.github.kongzhongtitian.RecipesAPI.recipe.ModRecipes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("recipesapi")
public class RecipesAPI {
   public static final String MODID = "recipesapi";

   public RecipesAPI() {
      IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
      ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);
      ModRecipes.RECIPE_TYPES.register(modEventBus);
      MinecraftForge.EVENT_BUS.register(this);
   }
}
