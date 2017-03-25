using System.Collections.Generic;
using UnityEngine;

public class GameWorld : MonoBehaviour {

    public static GameWorld instance;

    public int width, height;
    public float size;

    private Dictionary<Point, Transform> pointToTransform = new Dictionary<Point, Transform>();
    private Dictionary<Transform, Point> transformToPoint = new Dictionary<Transform, Point>();

    void Awake()
    {
        instance = this;
    }

    public void NotifyPosition(int x, int y, Transform transform)
    {
        Remove(transform);
        Point point = new Point(x, y);
        pointToTransform.Add(point, transform);
        transformToPoint.Add(transform, point);
    }

    public void Remove(Transform transform)
    {
        if (transformToPoint.ContainsKey(transform))
        {
            Point oldPoint = transformToPoint[transform];
            transformToPoint.Remove(transform);
            pointToTransform.Remove(oldPoint);
        }
    }

    public bool IsSquareFree(Point point)
    {
        return !pointToTransform.ContainsKey(point);
    }

    public bool IsValidCoordinate(Point point)
    {
        return IsValidCoordinate(point.x, point.y);
    }

    public bool IsValidCoordinate(int x, int y)
    {
        return 
            x >= 0 &&
            y >= 0 &&
            x < width &&
            y < height;
    }

}
