-- X-Plane Manager-dmg-setup.scpt
-- jpackage DMG customization script
-- Adapted from javapackager's customize-dmg.applescript.vtl
-- Uses javapackager default layout values (MacConfig.setDefaults)
--   windowX=10, windowY=60, windowWidth=540, windowHeight=360
--   iconSize=128, textSize=16
--   iconX=52, iconY=116, appsLinkIconX=360, appsLinkIconY=116
--
-- jpackage replaces these placeholders before running:
--   DEPLOY_VOLUME_URL, DEPLOY_VOLUME_PATH, DEPLOY_BG_FILE,
--   DEPLOY_INSTALL_LOCATION, DEPLOY_INSTALL_LOCATION_DISPLAY_NAME,
--   DEPLOY_TARGET

tell application "Finder"
	set theDisk to a reference to (disks whose URL = "DEPLOY_VOLUME_URL")
	open theDisk

	set theWindow to a reference to (container window of disks whose URL = "DEPLOY_VOLUME_URL")

	set current view of theWindow to icon view
	set toolbar visible of theWindow to false
	set statusbar visible of theWindow to false

	-- Window bounds: javapackager defaults (X=10, Y=60, W=540, H=360)
	set the bounds of theWindow to {10, 60, 550, 420}

	set theViewOptions to a reference to the icon view options of theWindow
	set arrangement of theViewOptions to not arranged
	set icon size of theViewOptions to 128
	set text size of theViewOptions to 16
	set background picture of theViewOptions to POSIX file "DEPLOY_BG_FILE"

	-- Create alias for install location (Applications)
	do shell script "(cd 'DEPLOY_VOLUME_PATH' && ln -s 'DEPLOY_INSTALL_LOCATION' 'DEPLOY_INSTALL_LOCATION_DISPLAY_NAME')"

	set allTheFiles to the name of every item of theWindow
	repeat with theFile in allTheFiles
		set theFilePath to POSIX path of theFile
		set appFilePath to POSIX path of "/DEPLOY_TARGET"
		if theFilePath ends with "DEPLOY_INSTALL_LOCATION_DISPLAY_NAME" then
			-- Position Applications folder: javapackager defaults (X=360, Y=116)
			set position of item theFile of theWindow to {360, 116}
		else if theFilePath ends with appFilePath then
			-- Position app icon: javapackager defaults (X=52, Y=116)
			set position of item theFile of theWindow to {52, 116}
			-- Hide extension
			set the extension hidden of item theFile of theWindow to true
		end if
	end repeat

	-- Force saving of the size
	close (get window of theDisk)
	open theDisk

	delay 1

	-- Adjust bounds to force Finder to save layout
	set the bounds of theWindow to {10, 60, 540, 410}
	delay 1
	set the bounds of theWindow to {10, 60, 550, 420}

	-- Wait for .DS_Store to be written
	delay 3

	update theDisk without registering applications
	close (get window of theDisk)
end tell