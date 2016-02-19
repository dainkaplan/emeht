# Emeht ('theme' backwards, or, I dare say, _inverted_!)

Inverts colours in a colour scheme of some kind. The tool you never knew you even wanted or needed!

Mainly to let you, after you have inverted your screen colours, to make an inverted version of your favourite theme to return your coding window to how it originally looked.


## UH... WHY??

Why would you want to invert a colour theme on a colour-inverted screen? Good question... Mostly for coding in pitch-black environments when you have to switch between many programs, some of which have light backgrounds by necessity. 

In particular, many international flights have a period of lights-out time that makes for _very dark_ (virtually no) lighting. If you are only coding, then simply a dark colour theme will be fine. However, if you need to switch to other apps, such as a web browser or a PDF etc. that has a light-coloured background by necessity, it is downright painful to look at. OS X has a handy feature of inverting the colours on screen (needs to be enabled, but CTRL+OPT+CMD+8), and there is also Nocturne/Tranquility for OS X that inverts colours _and_ prevents glaring brightness. But!!!! If you invert the colours, you'll need to select a light colour scheme to get back to a dark theme, but since the colours are now inverted, the theme does not look as intended; in fact, it usually looks pretty horrible; enter Emeht, who will take your originally dark-colour theme, invert it to be a light colour theme so that when you invert your screen colours, you get back your _original_, intended colour theme for happy coding in those pitch-black rooms!

## Supported color settings files

Clone the repo, and then follow specific instructions for different colour theme file types. In all cases, it will outpupt a file in the pwd that is the original file name with ".emeht" sandwiched in before the extension, e.g. "settings.jar" becomes "settings.emeht.har".
 
### Intellij settings.jar files

    sbt 'run --settings_type intellij path/to/some/settings.jar'

For Intellij, it will create "Theme (emeht)" versions of all the themes within the settings.jar file. If you have 4 themes in the jar, it will produce 4 themes in the output jar, all will be inverted with " (emeht)" appended to their names.
