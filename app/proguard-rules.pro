# Project-specific R8 keep rules, applied on top of the bundled defaults when a build type sets
# minifyEnabled = true. Nimbus reads JSON through kotlinx.serialization's generated serializers, which
# need their companion `serializer()` entry points kept when shrinking:
-keepclassmembers class com.example.nimbus.data.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.nimbus.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}
