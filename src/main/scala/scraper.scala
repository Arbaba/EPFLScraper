package main

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import java.io._
import scala.util.matching.Regex
import java.nio.charset.StandardCharsets._

object Main{

  val browser = JsoupBrowser()

  //Page containing a form where the user chooses the academic year, section and cycle
  val courses_url = ("http://isa.epfl.ch/imoniteur_ISAP/!GEDPUBLICREPORTS.filter?ww_b_list=1&ww_i_reportmodel=1715636965&ww_c_langue=&ww_i_reportModelXsl=1715637059")

  def main(args: Array[String]): Unit = {

    val file = new File("courses.json")
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(iterableToJson(allCourses, "courses", "name"))
    bw.close()
  }

  //Returns the list of all courses
  def allCourses:  Set[String] = {

    val courses_form = browser.get(courses_url)

    //Option tags grouped by select
    val options = (courses_form >> elementList("select")).map(_.children)
    val years :: section :: cycle :: Nil  = options.map(_.toList.map(x => (x >> attr("value") , x >> allText("option"))))
    val courses = for (y <- List(years.last); s <- section; c <- cycle if (c._2.contains("Semestre") && !s._2.contains("Mobilité") && !s._2.contains("Passerelle")))
                  yield findCourses(y, s, c)
    courses.reduce((a :Set[String],b : Set[String]) => a union b)
  }

  //Returns the list of courses given the specified parameters
  def findCourses(year : (String, String), section : (String, String), cycle: (String, String)): Set[String] = {
    val params = Map("ww_x_PERIODE_ACAD" -> year._1,
                     "zz_x_PERIODE_ACAD" -> year._2,
                     "ww_x_SECTION" -> section._1,
                     "zz_x_SECTION" -> section._2 ,
                     "ww_x_BLOC" -> cycle._1,
                      "zz_x_BLOC"  -> cycle._2 )
    val withCourses = browser.get(constructUrl(courses_url,params, "&dummy=ok"))
    (withCourses >> elementList(".ww_x_ITEMPLAN")).map(_ >> allText).map(encode_utf8).toSet

  }

  //Returns the URL with added parameters
  def constructUrl(url : String, params: Map[String, String], additional: String = ""): String = {
    var target : StringBuilder = new StringBuilder("http://isa.epfl.ch/imoniteur_ISAP/!GEDPUBLICREPORTS.filter?ww_b_list=1&ww_i_reportmodel=1715636965&ww_c_langue=&ww_i_reportModelXsl=1715637059&")
    params.foreach{case(k ,v) => target.append("&" + k + "=" + v)}
    target.append(additional)
    target.toString()
  }

  //Transforms an Iterable of String to the Json format
  def iterableToJson(courses : Iterable[String], collectionName: String, identifier: String) : String ={
    val json = new StringBuilder("{ \"" + collectionName + "\" : [")

    if(!courses.isEmpty){
      json.append("{\"name\" : \""+ courses.head +"\"}")
      courses.tail.foreach(s => json.append(",{\""+ identifier+ "\" : \""+ s +"\"}"))
    }
    json.append("]}")
    json.toString
  }

  def encode_utf8(a: String): String = new String(a.getBytes(ISO_8859_1), UTF_8)

}