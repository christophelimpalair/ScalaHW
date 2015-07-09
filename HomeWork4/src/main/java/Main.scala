import scala.collection._

/**
 * Created by Christophe on 3/10/15.
 */
object ScalaHW {
  // #1
  // Returns a map of indexes of all characters in a given string.
  // SortedSet returns a mutable ordered TreeSet
  def indexes (s: String) : mutable.Map[Char, mutable.SortedSet[Int]] = {
    val result = mutable.Map[Char, mutable.SortedSet[Int]]()
    s.zipWithIndex.foreach {
      case (c, i) => {
        if (result.contains (c))
          result (c) += i
        else
          result (c) = mutable.SortedSet (i)
      }
    }
    return result
  }

  // #2
  // takes in a string and gives us a map with sets that lists a letter's indexes in ascending order
  // zipWithIndex zips the string into a Vector paired with its index. Group by creates a map of
  // Vectors and ._1 sets the character as a key
  // mapValues(_.map(_._2)) modified the Vectors to only display index numbers in the values
  def indexes2(str: String) = {
    str.zipWithIndex.groupBy(_._1) mapValues (_.map(_._2).toSet)
//    str.zipWithIndex.groupBy(_._1) mapValues (_.map(_._2).toSet)
  }

  // #3
  def DimensionalArray(arr: Array[Double], col: Int) = {
    // Right after assigning it is non-empty.
    // Iterators in scala are single-used - once you traverse it becomes empty
    arr.iterator grouped(col) toArray
  }

  // #10
  // leafSum computes the sum of all elements in leaves.
  // Using pattern matching, we differentiate between Lists and Ints
  def leafSum(list: List[Any]): Int =
    list match {
      // this case is checking for a List
      // recursively check the first element in our list and add that to
      // a recursive call for the rest of our elements
      case List(x: List[Any], remains @ _*) => leafSum(x) + leafSum(remains.toList) // _* matches against a sequence without saying how long it is
      // this case is looking for the first parameter to be an Int
      // we then take that Int and add it to the result of a recursive call
      // that checks the remaining items in our list
      case List(x: Int, remains @ _*) => x + leafSum(remains.toList) // @ binds an identifier (remains) to the rest of our matched List (_*)
      // no maches
      case _ => 0
  }

  // #11
  sealed abstract class BinaryTree
  case class Leaf(value: Int) extends BinaryTree
  case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree
  def BTreeSum(tree: BinaryTree): Int =
  tree match {
    case Leaf(value) => value
    case Node(left,right) => BTreeSum(left) + BTreeSum(right)
  }

  def main(string: Array[String]) = {
//    println(DimensionalArray(Array(1,2,3,4), 2))
    val tree = Node(Node(Leaf(2),Leaf(5)), Node(Leaf(3),Leaf(8)))
    println(BTreeSum(tree))
  }
}
