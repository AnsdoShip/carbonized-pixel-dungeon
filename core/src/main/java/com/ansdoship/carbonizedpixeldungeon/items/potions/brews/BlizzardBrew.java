/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.ansdoship.carbonizedpixeldungeon.items.potions.brews;

import com.ansdoship.carbonizedpixeldungeon.Assets;
import com.ansdoship.carbonizedpixeldungeon.Dungeon;
import com.ansdoship.carbonizedpixeldungeon.actors.blobs.Blizzard;
import com.ansdoship.carbonizedpixeldungeon.actors.blobs.Blob;
import com.ansdoship.carbonizedpixeldungeon.items.potions.AlchemicalCatalyst;
import com.ansdoship.carbonizedpixeldungeon.items.potions.PotionOfFrost;
import com.ansdoship.carbonizedpixeldungeon.scenes.GameScene;
import com.ansdoship.carbonizedpixeldungeon.sprites.ItemSpriteSheet;
import com.ansdoship.pixeldungeonclasses.noosa.audio.Sample;

public class BlizzardBrew extends Brew {

	{
		image = ItemSpriteSheet.BREW_BLIZZARD;
	}

	@Override
	public void shatter(int cell) {
		if (Dungeon.level.heroFOV[cell]) {
			splash( cell );
			Sample.INSTANCE.play( Assets.Sounds.SHATTER );
			Sample.INSTANCE.play( Assets.Sounds.GAS );
		}
		
		GameScene.add( Blob.seed( cell, 1000, Blizzard.class ) );
	}

	@Override
	public int value() {
		//prices of ingredients
		return quantity * (30 + 40);
	}
	
	public static class Recipe extends com.ansdoship.carbonizedpixeldungeon.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{PotionOfFrost.class, AlchemicalCatalyst.class};
			inQuantity = new int[]{1, 1};

			cost = 3;

			output = BlizzardBrew.class;
			outQuantity = 1;
		}

	}
}
