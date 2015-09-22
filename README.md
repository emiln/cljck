# Cljck

> An OS-agnostic Clojure library for automating keyboard and mouse actions.

Cljck provides two distinct ways of automating various actions related to
keyboard, mouse, and the visual content on your screen.

1. This is a regular library, which can be used as you write your own Clojure
or Java program to solve specific tasks.

2. The JAR file resulting from building the library can process script files
written in EDN, which expose the most generally useful functions of the library
in a way where you don't have to compile any code to write small scripts. You
simply pass text files to the JAR on the command line.

You may at this point be left with a few questions. Even if you're not, I took
the liberty of making some up.

> __Why didn't you just use AutoIT?__
>
> It's not available on my favorite OS and I didn't want to learn a new
> language.

> __Do you expect me to learn your strange DSL?__
>
> You can use it as a library to write Clojure/Groovy/Java/etc. programs or you
> can treat it as an interpreter of a simple DSL. If both options sound
> unappealing, you're probably not the intended consumer of this product.

## Requirements

You will need a Java Runtime Environment of version 1.8. Will it work with Java
1.7? I have no idea. Will it work with Java 1.6? Absolutely not.

If you want to build the project yourself you will need
[Boot](http://boot-clj.com/).

## Motivation

I need to beat friends and family in Clicker Heroes. This is proving an
excellent game for driving progressive enhancements of Cljck, as there's
always one more thing you could be automating. Ideally it will eventually be
possible to handle everything including ascensions, and I can just leave a
laptop playing the game indefinitely until my accumulated Hero Souls make my
friends weep with frustrated jealousy.

## Library Usage

Just require the project from Clojars.

__Note__: It's not actually on Clojars yet.

## DSL Usage

Assuming you have the executable `cljck-x.y.z.jar` in your current directory,
and have written the script `click-the-stuff.edn` and placed it in the same
directory, just run

```
java -jar cljck-x.y.z.jar click-the-stuff.edn
```

in your terminal emulator. Passing multiple files will run all the scripts,
interleaving their execution in non-deterministic sequence. This is usually
perfectly fine, though, and is precisely how I use it.

### DSL API

Each EDN file should contain exactly one top-level element. Why not two?
Because right now it will only process the last top-level expression found due
to laziness on my part.

Every expression should be a vector starting with a keyword and containing zero
or more arguments. The keyword will serve as the name of a function to call,
and the remaining arguments will be passed to this function. The keywords and
their possible argument arities are as follows:

Keyword | Arguments | Description
--- | --- | ---
`:click` | | Clicks the left mouse button.
`:if` | `condition` `if-expression` `else-expression` | Evaluates the condition. If it is true, process the if-expression. If not, process the then-expression.
`:move-to` | `x` `y` | Moves the mouse cursor to position [x, y].
`:pointer-near` | `x` `y` `distance` | Evalutes to true if the mouse cursor is currently withing the given distance of the point [x, y].
`:press` | `key-string` | Presses the keyboard key given by the key string. See [Oracle's documentation](https://docs.oracle.com/javase/7/docs/api/java/awt/AWTKeyStroke.html#getAWTKeyStroke%28java.lang.String%29) about the specifics of this format.
`:repeat` | `n` `expression+` | One or more expressions should be supplied. These will be repeated n times in the sequence they appear.
`:repeatedly` | `expression+` | One or more expressions should be supplied. These will be repeated indefinitely in the sequence they appear.
`:scroll-down` | `n` | Scrolls down n ticks of the mouse wheel. You can omit the argument, which will be interpreted as meaning 1 tick.
`:scroll-up` | `n` | Scrolls up n ticks of the mouse wheel. You can omit the argument, which will be interpreted as meaning 1 tick.
`:wait` | `n` | Pauses execution of the script for roughly n miliseconds.
`:when` | `condition` `expression+` | Evalutes the condition. If it is true, processes all expressions following the condition. If not, does nothing.

### Example

Here's an example using all the keywords, but not making much sense.

```clojure
[:repeatedly
 [:if [:pointer-near 100 100 50]
  [:move-to 500 500]
  [:move-to 100 100]]
 [:repeat 100
  [:click]
  [:scroll-down 5]]
 [:when [:pointer-near 500 500 50]
  [:scroll-up]]
 [:wait 1000]]
 ```
 
 It reads as follows.
 
Repeat the following indefinitely:

  * Is the mouse cursor within 50 pixels of the point [100, 100]?
  
    Yes: Move it to [500, 500].
    No: Move it to [100, 100].
  
  * Repeat 100 times:
      * Click the left mouse button.
      * Scroll down 5 ticks.
  * Is the mouse cursor within 50 pixels of the point [500, 500]?
  
    Yes: Scroll up 1 tick.
  
  * Wait for 1000 miliseconds.
