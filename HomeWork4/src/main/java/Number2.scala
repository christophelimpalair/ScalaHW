/**
 * Created by Christophe on 3/10/15.
 */
object Number2 {
  def indexes2(str: String) = {
    str.zipWithIndex
  }

  def main(args: Array[String]) {
    println(indexes2("Mississippi"))
  }
}
