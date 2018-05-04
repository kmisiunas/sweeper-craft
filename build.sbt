import android.Keys._

androidBuild

platformTarget in Android := "android-23"

scalaVersion := "2.11.8"

name := "MC-Sweeper"

// eeror fix: http://stackoverflow.com/questions/34771801/proguard-fails-when-updating-to-android-sdk-23
proguardOptions ++= Seq("-dontwarn com.google.android.gms.**")

useProguard := true

proguardOptions ++= Seq("-dontobfuscate", "-dontoptimize")

proguardVersion := "5.2"


javacOptions in Compile ++= Seq("-source", "1.7", "-target", "1.7")

//showSdkProgress in Android := false

//libraryDependencies += "com.google.android.gms" % "play-services" % "VERSION"

libraryDependencies += "com.google.android.gms" % "play-services" % "9.6.0"

libraryDependencies += "com.google.firebase" % "firebase-ads" % "9.6.0"

//libraryDependencies += "com.google.firebase" % "firebase-core" % "9.6.0"

libraryDependencies += "com.android.support" % "appcompat-v7"  % "24.2.0"

libraryDependencies += "com.google.gms" % "google-services" % "3.0.0"


libraryDependencies += "org.greenrobot" % "eventbus" % "3.0.0"


// https://android-arsenal.com/details/1/4253#!description
libraryDependencies += "com.github.vihtarb" % "tooltip" % "0.1.8"

resolvers += Resolver.bintrayRepo("matthewtamlin", "maven")
resolvers += Resolver.jcenterRepo

// tutorial page
libraryDependencies += "com.matthew-tamlin" % "sliding-intro-screen" % "3.2.0"


// there is confusion on how the package gets packed. found this useful
// https://github.com/scala-android/sbt-android/issues/161
// http://scala-on-android.taig.io/proguard/
// http://scala-on-android.taig.io/

googleServicesSettings


dexMaxHeap in Android := "4048m"

dexMulti in Android := true

proguardScala in Android := true

useProguard in Android := true

// from build suggestions
//dexMainClassesConfig := baseDirectory.value / "copy-of-maindexlist.txt"
