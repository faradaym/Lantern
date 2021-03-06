package lantern

import org.scala_lang.virtualized.virtualize
import org.scala_lang.virtualized.SourceContext

import scala.virtualization.lms.common._

trait ScannerOps extends Base with UncheckedOps {
  def open(name: Rep[String]): Rep[Int]
  def close(fd: Rep[Int]): Rep[Unit]
  def filelen(fd: Rep[Int]): Rep[Int]
  def mmap[T:Manifest](fd: Rep[Int], len: Rep[Int]): Rep[Array[T]]
  def stringFromCharArray(buf: Rep[Array[Char]], pos: Rep[Int], len: Rep[Int]): Rep[String]
  def prints(s: Rep[String]): Rep[Int]
  def infix_toInt(c: Rep[Char]): Rep[Int] = c.asInstanceOf[Rep[Int]]

  def openf(name: Rep[String], mode: Rep[String]): Rep[Long] // return a file pointer
  def closef(fp: Rep[Long]): Rep[Unit]
  def getFloat(fp: Rep[Long], data: Rep[Array[Float]], i: Rep[Int]): Rep[Unit]
  //def getFloat(fp: Rep[Long], at: Rep[Float]): Rep[Unit]
  def getInt(fp: Rep[Long], at: Rep[Int]): Rep[Unit]
  def getInt(fp: Rep[Long], data: Rep[Array[Int]], i: Rep[Int]): Rep[Unit]
  def fprintf(fp: Rep[Long], format: Rep[String], content: Rep[Any]): Rep[Unit]
  def fprintf(fp: Rep[Long], format: Rep[String], content1: Rep[Any], content2: Rep[Any]): Rep[Unit]
  def fprintf(fp: Rep[Long], format: Rep[String], content1: Rep[Any], content2: Rep[Any], content3: Rep[Any]): Rep[Unit]
}

trait ScannerOpsExp extends ScannerOps with UncheckedOpsExp {
  def remap[T:Manifest] = manifest[T] match {
    case x if x == manifest[Float] => "float"
    case x if x == manifest[Int] => "int"
    case x if x == manifest[Char] => "char"
    case _ => throw new Exception(s"${manifest[T]} todo")
  }
  def open(path: Rep[String]) = uncheckedPure[Int]("open(",path,",0)")
  def filelen(fd: Rep[Int]) = uncheckedPure[Int]("fsize(",fd,")") // FIXME: fresh name
  def mmap[T:Manifest](fd: Rep[Int], len: Rep[Int]) = uncheckedPure[Array[T]]("(",remap(manifest[T]),"*)mmap(0, ",len,", PROT_READ | PROT_WRITE, MAP_FILE | MAP_PRIVATE, ",fd,", 0)")
  def stringFromCharArray(data: Rep[Array[Char]], pos: Rep[Int], len: Rep[Int]): Rep[String] = uncheckedPure[String](data,"+",pos)
  def close(fd: Rep[Int]) = unchecked[Unit]("close(",fd,")")
  def prints(s: Rep[String]): Rep[Int] = unchecked[Int]("printll(",s,")")

  def openf(name: Rep[String], mode: Rep[String]) = unchecked[Long]("(long)fopen(", name, ", ", mode, ")")
  def closef(fp: Rep[Long]) = unchecked[Unit]("fclose((FILE*)", fp, ")")
  def getFloat(fp: Rep[Long], data: Rep[Array[Float]], i: Rep[Int]) =
    unchecked[Unit]("if (fscanf((FILE *)", fp, ",\"%f\", &", data, "[", i, "]", ")!=1) perror(\"Error reading file\")")
  //def getFloat(fp: Rep[Long], at: Rep[Float]) =
  //  unchecked[Unit]("if (fscanf((FILE *)", fp, ",\"%f\", &", at, ")!=1) perror(\"Error reading file\")")
  def getInt(fp: Rep[Long], at: Rep[Int]) =
    unchecked[Unit]("if (fscanf((FILE *)", fp, ",\"%d\", &", at, ")!=1) perror(\"Error reading file\")")
  def getInt(fp: Rep[Long], data: Rep[Array[Int]], i: Rep[Int]) =
    unchecked[Unit]("if (fscanf((FILE *)", fp, ",\"%d\", &", data, "[", i, "]", ")!=1) perror(\"Error reading file\")")

  // for writing to file in c
  def fprintf(fp: Rep[Long], format: Rep[String], content: Rep[Any]) =
    unchecked[Unit]("fprintf((FILE *)", fp, ", ", format, ", ", content, ")")
  def fprintf(fp: Rep[Long], format: Rep[String], content1: Rep[Any], content2: Rep[Any]) =
    unchecked[Unit]("fprintf((FILE *)", fp, ", ", format, ", ", content1, ", ", content2, ")")
  def fprintf(fp: Rep[Long], format: Rep[String], content1: Rep[Any], content2: Rep[Any], content3: Rep[Any]) =
    unchecked[Unit]("fprintf((FILE *)", fp, ", ", format, ", ", content1, ", ", content2, ", ", content3, ")")

}

trait CGenScannerOps extends CGenUncheckedOps

//fscanf(fp, "%f", &mgone[i]) != 1
//FILE* fp = fdopen(fd, "w");
