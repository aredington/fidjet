# Fidjet

Fidjet is dead-simple ad-hoc configuration for pure functions, inspired by suggestions made by Stuart Sierra on this blog post: http://cemerick.com/2011/10/17/a-la-carte-configuration-in-clojure-apis/. If you write a core API where functions accept some config parameter as their first argument, you can then create a parallel namespace where those same functions can be used in a with-config block.

Suppose you had a namespace monotony.core, in it you had a function named periods, which needed a configuration as its first argument and a keyword as its second. Its definition might look something like this:

    (def periods [config keyword] (some-awesome-functional-logic))

Every time you called this you'd have to pass in config explicitly. Annoying! Further, if you were composing many such functions that all shared the same config, even more annoying! One nice solution would be to clone this API to another namespace that allowed a with-config block. Inside that with-config block we could play to our hearts' content with that implicit configuration. This is easy, if you'd like to use fidjet it will require about 4 lines of clojure. If your main API resides in monotony.core, make a new file named configured.clj, and write the following:

    (ns monotony.configured
      (:require [monotony.core :as m]
                [fidjet.core :as f]))
    (f/remap-ns-with-arg monotony.core config))

Boom. You're done.

## How does it work?

Fidjet expects two arguments to remap-ns-with-arg: the source namespace (ns) and the symbol of the configuration argument (arg-sym). It also makes use of the namespace in which it is run as the target namespace (target-ns) What it does next is:

* Dig through the source-ns finding all the public functions. It examines their arglists. All of the functions which accept arg-sym as their first argument get rebound in target-ns to throw an exception if they are not in a with-arg-sym block.
* All of the functions which did not accept arg-sym as their first argument get rebound in target-ns untouched.
* A new macro is created in target-ns. If your arg-sym is config, it will be called with-config. If your arg-sym is connection, it will be called with-connection. This macro accepts two arguments, the arg you want to share across a body, and the body itself. It rebinds all of the previously imported functions which were made to throw exceptions on call to partial versions usings the shared argument. It then executes body. 

Picking on monotony again, you can now do the following:

    (require ['monotony.configured :as 'm])
    (m/with-config (m/new-config) (take 3 (m/periods :month)))

This is semantically identical to writing:

    (take 3 (m/periods (m/new-config) :month))

But when the body starts to get large, will save you a lot of typing.

## It doesn't work right?

Sorry! If you can show me how it's breaking, I'll try and fix it. If you think you can fix it, fork the repo, describe the problem, add your fix, and send me a pull request. I'll be much more likely to accept your pull request if you also include tests that show how fidjet is broken and how your proposed changes fix it.
