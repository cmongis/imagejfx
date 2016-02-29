= Simple query : 

== By metadata :

 - *channel* = *3*
 - *Z* > *3*
 - *file name* contains *stack*

== By tag 

 - tagged with: *control*,*valid* 


== By file (not available yet)

 - coming from *stack* (will select all the plane coming from files that contains the word stack)
 - coming from folder *Z:/Data/processed*

== By date (not available yet)

 - earlier than *23/03/2014*
 - older than *23/01/2015*
 - on the *23/04/1987*

You can aggregate simple queries using the keywords *AND* and *OR* :

 - *Z* > *2* AND *Z* <= *6*
 - *channel name* contains *gfp* or *channel name* contains *mcherry*
 - earlier than *23/04/1987* and *size* > *20*