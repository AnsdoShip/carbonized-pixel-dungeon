package com.ansdoship.carbonizedpixeldungeon.ios;

import com.ansdoship.carbonizedpixeldungeon.CarbonizedPixelDungeon;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.ansdoship.carbonizedpixeldungeon.PDSettings;
import com.ansdoship.pixeldungeonclasses.noosa.Game;
import com.ansdoship.pixeldungeonclasses.utils.PlatformSupport;

import org.robovm.apple.audiotoolbox.AudioServices;
import org.robovm.apple.systemconfiguration.SCNetworkReachability;
import org.robovm.apple.systemconfiguration.SCNetworkReachabilityFlags;
import org.robovm.apple.uikit.UIApplication;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IOSPlatformSupport extends PlatformSupport {
	@Override
	public void updateDisplaySize() {
		//non-zero safe insets on left/top/right means device has a notch, show status bar
		if (Gdx.graphics.getSafeInsetTop() != 0
				|| Gdx.graphics.getSafeInsetLeft() != 0
				|| Gdx.graphics.getSafeInsetRight() != 0){
			UIApplication.getSharedApplication().setStatusBarHidden(false);
		} else {
			UIApplication.getSharedApplication().setStatusBarHidden(true);
		}

		if (!PDSettings.fullscreen()) {
			int insetChange = Gdx.graphics.getSafeInsetBottom() - Game.bottomInset;
			Game.bottomInset = Gdx.graphics.getSafeInsetBottom();
			Game.height -= insetChange;
			Game.dispHeight = Game.height;
		} else {
			Game.height += Game.bottomInset;
			Game.dispHeight = Game.height;
			Game.bottomInset = 0;
		}
		Gdx.gl.glViewport(0, Game.bottomInset, Game.width, Game.height);
	}

	@Override
	public void updateSystemUI() {
		int prevInset = Game.bottomInset;
		updateDisplaySize();
		if (prevInset != Game.bottomInset) {
			CarbonizedPixelDungeon.seamlessResetScene();
		}
	}

	@Override
	public boolean connectedToUnmeteredNetwork() {
		SCNetworkReachability test = new SCNetworkReachability("www.apple.com");
		return !test.getFlags().contains(SCNetworkReachabilityFlags.IsWWAN);
	}

	public void vibrate( int millis ){
		//gives a short vibrate on iPhone 6+, no vibration otherwise
		AudioServices.playSystemSound(1520);
	}

	/* FONT SUPPORT */

	//custom pixel font, for use with Latin and Cyrillic languages
	private static FreeTypeFontGenerator basicFontGenerator;
	//droid sans fallback, for asian fonts
	private static FreeTypeFontGenerator asianFontGenerator;

	@Override
	public void setupFontGenerators(int pageSize, boolean systemfont) {
		//don't bother doing anything if nothing has changed
		if (fonts != null && this.pageSize == pageSize && this.systemfont == systemfont){
			return;
		}
		this.pageSize = pageSize;
		this.systemfont = systemfont;

		resetGenerators(false);
		fonts = new HashMap<>();

		if (systemfont) {
			basicFontGenerator = asianFontGenerator = fallbackFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/droid_sans.ttf"));
		} else {
			basicFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/pixel_font.ttf"));
			asianFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/fusion_pixel.ttf"));
			fallbackFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/droid_sans.ttf"));
		}

		fonts.put(basicFontGenerator, new HashMap<>());
		fonts.put(asianFontGenerator, new HashMap<>());
		fonts.put(fallbackFontGenerator, new HashMap<>());

		packer = new PixmapPacker(pageSize, pageSize, Pixmap.Format.RGBA8888, 1, false);
	}

	private static final Matcher asianMatcher = Pattern.compile("\\p{InHangul_Syllables}|" +
			"\\p{InCJK_Unified_Ideographs}|\\p{InCJK_Symbols_and_Punctuation}|\\p{InHalfwidth_and_Fullwidth_Forms}|" +
			"\\p{InHiragana}|\\p{InKatakana}").matcher("");

	@Override
	protected FreeTypeFontGenerator getGeneratorForString( String input ){
		if (asianMatcher.reset(input).find()){
			return asianFontGenerator;
		} else {
			return basicFontGenerator;
		}
	}

	@Override
	public boolean isAndroid() {
		return false;
	}

	@Override
	public boolean isiOS() {
		return true;
	}

	@Override
	public boolean isDesktop() {
		return false;
	}

}
