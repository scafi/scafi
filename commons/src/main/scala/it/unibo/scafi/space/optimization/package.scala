package it.unibo.scafi.space

package object optimization {
  implicit class RichPoint3D(p : Point3D) {
    private val Dim = 3
    def copy: Point3D = Point3D(p.x,p.y,p.z)

    def *(a: Double): Point3D = Point3D(p.x*a,p.y*a,p.z*a)

    def -(p2: Point3D): Point3D = Point3D(p.x - p2.x, p.y - p2.y, p.z - p2.z)

    def === (p2 : Point3D): Boolean = p.x == p2.x && p.y == p2.y && p.z == p2.z

    def apply(index : Int) : Double = {
      require(index < Dim)
      index match {
        case 0 => p.x
        case 1 => p.y
        case 2 => p.z
      }
    }
    def update(index : Int, v : Double): Point3D = {
      require(index < Dim)
      index match {
        case 0 => Point3D(v,p.y,p.z)
        case 1 => Point3D(p.x,v,p.z)
        case 2 => Point3D(p.x,p.y,v)
      }
    }
  }
}
