# clojure-datomic

A Clojure project designed to make the tests related to Alura's datomic course. Each branch shall refer to a course's
module, and each module shall have it's own focus.

##Installing Datomic

To install datomic there are two steps:

* 1. Download Datomic server;
    
* 2. Download Datomic's library (.jar) to work in this code;
    
###About Step One

* 1. The first step can be accomplished going into Datomic's website and downloading it the [**Starter** version](https://www.datomic.com/get-datomic.html) 

* 2. Now, you're going to need a Datomic account. After the download, go to the [account page](https://my.datomic.com/account) 
     and click in **Send License Key**. You'll received it in your e-mail;
    
* 3. Unpack the downloaded file;
    
* 4. Open the file inside **bin/transactor config/samples/free-transactor-template.properties**;
    
* 5. Add the license-key line you've received in your e-mail into this file;
    
* 6. Copy this file from **/samples/** to **/transactor config/**;
    
* 7. Go to Datomic's folder root and run **bin/transactor config/free-transactor-template.properties** - it should show 
     something as bellow:

```cmdline
Launching with Java options -server -Xms1g -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=50
Starting datomic:free://localhost:4334/<DB-NAME>, storing data in: data ...
System started datomic:free://localhost:4334/<DB-NAME>, storing data in: data
```

###About Step Two

* 1. Go back to the [account page](https://my.datomic.com/account);
    
* 2. In your computer, go to **~/.lein** directory (where lein is installed) and create a file 
     **called credentials.clj.gpg**;

* 3. Add in that file the following (as explained in the account page from the link above);

```cmdline
{#"my\.datomic\.com" {:username "williamporto19@gmail.com"
                      :password "029d796c-1692-4fad-ad17-9c664ea9ada4"}}
```

* 4. Install maven in your machine;
    
* 5. Go to the downloaded datomic's folder and run the following:

-> IMPORTANT: the command bellow will install the **.jar** that was downloaded with the datomic into your machine.
   The Datomic's documentation said we can install it only in the clojure's project, but I wasn't able to (nor the professor
   in the Alura's course, apparently).

```cmdline
bin/maven-install
```

* 6. After the **.jar** is installed, go to the project.clj file in the project and add the line with the .jar information 
     (you can see more about this in this commit - just go to the file in this project and see the line by yourself, it's
     the version of Datomic I used while making this)

## Usage

This project shall be organized in the following way:

* Each branch has a module from the course;

* Master branch shall have all the modules until that point in my studies;

* Every branch shall be merged into master via Pull Request;

## License

Copyright © 2021 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
