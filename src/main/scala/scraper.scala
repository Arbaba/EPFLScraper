package main

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import java.io._

object Main{

  val browser = JsoupBrowser()

  def main(args: Array[String]): Unit = {

    //Page containing a form where the user chooses the academic year, section and cycle
    val url = ("http://isa.epfl.ch/imoniteur_ISAP/!GEDPUBLICREPORTS.filter?ww_b_list=1&ww_i_reportmodel=1715636965&ww_c_langue=&ww_i_reportModelXsl=1715637059")

    val html = browser.get(url)

    val distinctChildren = (html >> elementList("select")).map(_.children)
    val years :: section :: cycle :: Nil  = distinctChildren.map(_.toList.map(_ >> allText("option")))

    val courses = for (y <- List("2018-2019"); s <- section.drop(20); c <- cycle if (c.contains("Semestre") || s.contains("Mobilité")))   yield findCourses(y, s, c)

    //Write courses in the file
    val file = new File("courses.txt")
    val bw = new BufferedWriter(new FileWriter(file))
    courses.foreach(x => bw.write(x + "\n"))
    bw.close()
  }


  def findCourses(y : String, s : String, c : String): List[String] = {
    val html = browser.get(constructUrl(y,s,c))
    (html >> elementList(".ww_x_ITEMPLAN")).map(_ >> allText)

  }

  def constructUrl(year : String, section : String, cycle : String): String = (
    "http://isa.epfl.ch/imoniteur_ISAP/!GEDPUBLICREPORTS.filter?ww_b_list=1&ww_i_reportmodel=1715636965&ww_c_langue=&ww_i_reportModelXsl=1715637059&"
    + "zz_x_PERIODE_ACAD=" + year + "&ww_x_PERIODE_ACAD=1866893861&"
    + "zz_x_SECTION="+ section
    + "&ww_x_SECTION=942293&"
    + "zz_x_BLOC="
    + cycle
    + "&ww_x_BLOC=6683117&dummy=ok"
    )

  def elementToText(e : net.ruippeixotog.scalascraper.model.Element) : String = e >> allText("a")
}