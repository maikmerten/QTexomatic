# QTexomatic

QTexomatic is a tool to combine and convert textures. It's particular focus is on creating textures for Quake, but the resulting textures can be used in other games, too, of course.

## Compiling QTexomatic

QTexomatic is written in Java and should work on any operating systems with a reasonably recent Java execution environment, e.g. Java 11. 

The support for Quake texture formats is provided by [QuakeTextureTool](https://github.com/maikmerten/QuakeTextureTool). If QuakeTextureTool is installed in your local maven repository (`mvn clean install`), then invoking `mvn clean package` in this directory should create an executable `jar`-archive with all necessary dependencies in the `target`-directory .


## Theory of operation

QTexomatic operates by executing a provided script. Basic operations are to load, manipulate and save textures.

Textures loaded in QTexomatic are referred to by symbolic names.Symbolic names can be chosen arbitrarily, but following symbolic names have special meaning when saving output:

* base: The "main"-part of the texture.
* glow: Parts of the texture that shall glow in the dark (Quake-speak: "fullbright"). These glowing pixels need special treatment.

An example-script is included in the `examples`-directory.

### Invoking QTexomatic

`java -jar QTexomatic-<VERSION>.jar /path/to/script.txt`


## Available operations


| command | action |
|---|---|
|`add <DEST> <SRC1> <SRC2>`| Add the RGB-pixel values of SRC1 and SRC2 and save the result as DEST.|
|`clearall`| Forget all loaded images. Free all symbolic names.|
|`combine <DEST> <SRC1> <SRC2>`| Layer SRC2 on top of SRC1, take alpha-channel into consideration. Save the result as DEST.|
|`copy <DEST> <SRC>`| The image referred to via SRC is thereafter also accessible under the symbolic name DEST.|
|`crop <DEST> <SRC> <X> <Y> <WIDTH> <HEIGHT>`| Create a sub-image of SRC from specified coordinates and save it as DEST.|
|`load <DEST> <FILE>`| Load image specified by FILE and store result in DEST. Pathnames are relative to the script file.|
|`new <DEST> <WIDTH> <HEIGHT> <HEXCOLOR>`| Create a new image with specified dimensions and fill with specified color in ARGB/RGB-hex-format (e.g. `#FF0000` for full red or `#80FF0000` for half-transparent full red). Save created image as DEST.|
|`replacecolor <DEST> <SRC> <HEXCOLOR1> <HEXCOLOR2>`| Replace all pixels in SRC matching HEXCOLOR1 with the color value of HEXCOLOR2 and save the result in DEST.|
|`replaceothercolors <DEST> <SRC> <HEXCOLOR1> <HEXCOLOR2>`| Replace all pixels in SRC **NOT** matching HEXCOLOR1 with the color value of HEXCOLOR2 and save the result in DEST.|
|`save <NAME> <FORMAT>`| Save "base"-texture (and if present, "glow"-texture) to the specified format. Formats are PNG, TGA, WAD (include texture in Quake-WAD-file) and PAK (include texture in TGA-format in Quake-PAK container-file). When saved to PAK, the prefix "textures/" is automatically added to the texture-name.|
|`scale <DEST> <SRC> <WIDTH> <HEIGHT>`| Resample the SRC-image to the new specified dimensions and save the result in DEST.|
|`scaleluma <DEST> <SRC1> <SRC2>`| Modify the per-pixel brightness of SRC1 according to the brightness of the respective pixels in SRC2. Pixels in SRC2 brighter than rgb(128,128,128) will increase the brigthness in SRC1, while pixels in SRC2 dimmer than rgb(128,128,128) will decrease the brightness in SRC1. The result is saved in DEST.|
|`set <VARNAME> <VALUE>`|Specify a new value for a specified variable. Confer list of available variables.|
|`sub <DEST> <SRC1> <SRC2>`| Subtract the RGB-pixel values of SRC2 from SRC1 and save the result as DEST.|
|`tile <DEST> <SRC> <XREPEAT> <YREPEAT>`|Repeat SRC XREPEAT-times in the X-direction and YREPEAT-times in the Y-direction and save the result in DEST.|

## Available variables

Some operations take predefined variables into account. The values of these variables can be modified using the `set` operation.

| variable | example | meaning |
|---|---|---|
| `offset` | `set offset 0 32` | Specify a relative X/Y-offset between SRC1 and SRC2 for operations working on two images. For instance, when combining two images, the position of the second image relative to the first image is specified via the "offset"-variable. Defaults to (0,0). |
| `pakfile` | `set pakfile textures.pak` | Specify the file-name for the output PAK-file. Defaults to "pak0.pak"|
|`waddither`| `set waddither 0.4` | Specifies the strength of the dithering applied to the image when converting to the 8-bit Quake-palette. Defaults to 0.25.|
|`wadfile`| `set wadfile textures.wad`| Specify the file-name for the outpu WAD-file. Defaults to "output.wad".|