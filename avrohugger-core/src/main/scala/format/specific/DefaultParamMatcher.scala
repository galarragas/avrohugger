package avrohugger
package format
package specific

import org.apache.avro.Schema
import org.apache.avro.Schema.Type

import treehugger.forest._
import definitions._
import treehuggerDSL._

import scala.collection.JavaConversions._


object DefaultParamMatcher {

  def asDefaultParam(classStore: ClassStore, avroSchema: Schema): Tree  = {

    avroSchema.getType match {

      case Type.BOOLEAN => TRUE
      case Type.INT     => LIT(1)
      case Type.LONG    => LIT(1L)
      case Type.FLOAT   => LIT(1F)
      case Type.DOUBLE  => LIT(1D)
      case Type.STRING  => LIT("")
      case Type.NULL    => NULL
      case Type.ARRAY   => LIST(asDefaultParam(classStore, avroSchema.getElementType))
      case Type.MAP     => MAKE_MAP(LIT("") ANY_-> asDefaultParam(classStore, avroSchema.getValueType))
      case Type.FIXED   => sys.error("the FIXED datatype is not yet supported")
      case Type.ENUM    => NULL // TODO Take first enum value?
      case Type.BYTES   => sys.error("the BYTES datatype is not yet supported")
      case Type.RECORD  => NEW(classStore.generatedClasses(avroSchema))
      case Type.UNION   => {
        val unionSchemas = avroSchema.getTypes.toList
        // unions are represented as Scala Option[T], and thus unions must be of two types, one of them NULL
          if (unionSchemas.length == 2 && unionSchemas.exists(schema => schema.getType == Type.NULL)) {
            val maybeSchema = unionSchemas.find(schema => schema.getType != Type.NULL)
            if (maybeSchema.isDefined ) SOME(asDefaultParam(classStore, maybeSchema.get))
            else sys.error("no avro type found in this union")  
          }
          else sys.error("unions not yet supported beyond nullable fields")
      }
    }
  }

}
