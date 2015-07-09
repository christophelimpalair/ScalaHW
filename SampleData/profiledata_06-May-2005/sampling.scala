import scala.io.Source
import java.io._

val source = Source.fromFile("user_artist_data.txt")
val out = new PrintWriter("selected_user_artist_data.txt")
val lineIterator = source.getLines

var i = 0
for (j <- lineIterator){
   if(i< 1000000){
      out.println(j)    
   }
   if(i > 1300000){
        println("close")
	out.close()
	source.close()
	
   }
   i = i + 1
}

