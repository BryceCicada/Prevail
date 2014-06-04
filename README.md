Prevail
=======

[![Build Status](https://brycecicada.ci.cloudbees.com/buildStatus/icon?job=develop)](https://brycecicada.ci.cloudbees.com/job/develop/)

Prevail is an abstraction layer for your application data.

It is not an ORM framework, a persistence layer, a networking library or anything complicated.

It simply allows your applications to abstract away the sources of its data.  By using Prevail, access to your
data becomes asynchronous and event driven.  Importantly, the sources of data are decoupled from the logic of your application.

#### Motivations

Prevail is motivated by the following points:

* The majority of code in an application cares little about the source of the data on which it operates.
* Data should be event driven.  When stuff changes, you want to know about it.  It's not enough to just query an object.  We want to know when that object becomes invalid.
* Sources of data should be easily changed, without disrupting lots of code.  It should be easy to swap a network resource for a hashtable during testing.  Sure, that could be achieved by setting up fake test objects, but why bother when it should be easy to swap in new data sources quickly in the first place.  Such decoupled code is easier to change in the future too.
* It should be easy to perform data operations on multiple data sources at once.  A local cache query might return quickly, whilst a network query may take longer.  However, it should be possible for client code to be completely unaware that there are multiple sources of data.  It should be possible for a client merely to handle the data as it arrives.

In particular, on Android:

* Many people use ContentProviders for data simply because they give easy access to useful asynchronous functionality.  This can easily contaminate your application with Cursors, Uris, ContentObservers and CursorLoaders.  It's frustrating to have code that depends on these classes, when they should really never penetrate into the applications domain.  It's much nicer to have all the good stuff, like asynchronous loading and notification of change, within your own classes in your own domain.
* What happens when you realise that all the stuff you've been persisting to SharedPreferences needs to be in a database so that you can access it with a richer query language?  In that case, client code shouldn't have to change, only the means of data access needs to change.

#### Examples:

There is an example java application within the sources.  


#### Roadmap:

* Android Loaders that respond to Prevail events.
* Example android app.
