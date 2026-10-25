# Room y kotlinx.serialization incluyen sus propias reglas de R8.
# Estas reglas adicionales protegen los modelos del respaldo JSON.
-keepattributes *Annotation*, InnerClasses
-keepclassmembers @kotlinx.serialization.Serializable class com.pablozavfer.cuadernopredicas.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
