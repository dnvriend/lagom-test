package com.github.dnvriend.sbt

import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtScalariform
import de.heikoseeberger.sbtheader.HeaderKey._
import de.heikoseeberger.sbtheader.{AutomateHeaderPlugin, _}
import de.heikoseeberger.sbtheader._
import de.heikoseeberger.sbtheader.HeaderKey._
import de.heikoseeberger.sbtheader.license.Apache2_0
import com.typesafe.sbt.SbtScalariform
import com.lightbend.lagom.sbt._

import scalariform.formatter.preferences._

object GenericSettings extends AutoPlugin {
	
  override def requires = com.typesafe.sbt.SbtScalariform && LagomScala && AutomateHeaderPlugin

  object autoImport {   
  }

  import autoImport._
  import LagomPlugin.autoImport._

  override lazy val projectSettings = 
    SbtScalariform.scalariformSettings ++ genericSettings
	 
  def genericSettings: Seq[Setting[_]] = Seq(
  	headers := Map(
    "scala" -> Apache2_0("2016", "Dennis"),
    "conf" -> Apache2_0("2016", "Dennis", "#")
    ),

    SbtScalariform.autoImport.scalariformPreferences := SbtScalariform.autoImport.scalariformPreferences.value
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
    .setPreference(DoubleIndentClassDeclaration, true)

	// lagom configuration
//	  lagomServiceLocatorPort in ThisBuild := 8000
  )
}