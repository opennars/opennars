/**
 * Project : JHelpXML<br>
 * Package : jhelp.xml.io<br>
 * Class : XMLSerializable<br>
 * Date : 23 mai 2010<br>
 * By JHelp
 */
package jhelp.xml.io;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a field to be XML serializable <br>
 * <br>
 * Last modification : 23 mai 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(
{
   ElementType.FIELD
})
public @interface XMLSerializable
{
}