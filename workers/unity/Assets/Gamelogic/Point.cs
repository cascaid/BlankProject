using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Point  {

    public int x, y;

    public Point(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public override bool Equals(object obj)
    {
        if (obj == null) return false;
        if (!(obj is Point)) return false;
        Point other = obj as Point;
        return x == other.x && y == other.y;
    }

    public override int GetHashCode()
    {
        return x + 13 * y;
    }

    public override string ToString()
    {
        return string.Format("[x:{0},y:{1}]", x, y);
    }

    public Point Left() { return new Point(x - 1, y); }
    public Point Right() { return new Point(x + 1, y); }
    public Point Up() { return new Point(x, y + 1); }
    public Point Down() { return new Point(x, y - 1); }

    public IEnumerable<Point> Ordinates()
    {
        yield return Left();
        yield return Right();
        yield return Up();
        yield return Down();
    }

}
