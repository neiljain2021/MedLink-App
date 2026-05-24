# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the SDK's default ProGuard rules.

-keepattributes *Annotation*
-keep public class * extends android.app.Activity
-keep public class * extends androidx.fragment.app.Fragment
