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

package com.ansdoship.carbonizedpixeldungeon.windows;

import com.ansdoship.carbonizedpixeldungeon.Badges;
import com.ansdoship.carbonizedpixeldungeon.effects.BadgeBanner;
import com.ansdoship.carbonizedpixeldungeon.scenes.PixelScene;
import com.ansdoship.carbonizedpixeldungeon.ui.RenderedTextBlock;
import com.ansdoship.carbonizedpixeldungeon.ui.Window;
import com.ansdoship.pixeldungeonclasses.noosa.Image;

public class WndBadge extends Window {

	private static final int MAX_WIDTH = 125;
	private static final int MARGIN = 4;

	public WndBadge( Badges.Badge badge, boolean unlocked ) {

		super();

		Image icon = BadgeBanner.image( badge.image );
		icon.scale.set( 2 );
		if (!unlocked) icon.brightness(0.4f);
		add( icon );

		RenderedTextBlock title = PixelScene.renderTextBlock( badge.title(), 9 );
		title.maxWidth(MAX_WIDTH - MARGIN * 2);
		title.align(RenderedTextBlock.CENTER_ALIGN);
		title.hardlight(TITLE_COLOR);
		if (!unlocked) title.hardlight( 0x888822 );
		add(title);

		RenderedTextBlock info = PixelScene.renderTextBlock( badge.desc(), 6 );
		info.maxWidth(MAX_WIDTH - MARGIN * 2);
		info.align(RenderedTextBlock.CENTER_ALIGN);
		if (!unlocked) info.hardlight( 0x888888 );
		add(info);

		float w = Math.max( icon.width(), Math.max(title.width(), info.width()) ) + MARGIN * 2;

		icon.x = (w - icon.width()) / 2f;
		icon.y = MARGIN;
		PixelScene.align(icon);

		title.setPos((w - title.width()) / 2, icon.y + icon.height() + MARGIN);
		PixelScene.align(title);

		info.setPos((w - info.width()) / 2, title.bottom() + MARGIN);
		PixelScene.align(info);
		resize( (int)w, (int)(info.bottom() + MARGIN) );

		if (unlocked) BadgeBanner.highlight( icon, badge.image );
	}
}
