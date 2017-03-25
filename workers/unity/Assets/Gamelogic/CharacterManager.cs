using Improbable.Player;
using Improbable.Unity.Core;
using Improbable.Unity.Visualizer;
using System;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class CharacterManager : MonoBehaviour {

    [Require]
    public Character.Reader reader;
    [Require]
    public Authority.Writer writer;
    
    public Color authority, noAuthority;
    private Image sprite;
    private System.Random rand;

    void Awake()
    {
        sprite = GetComponent<Image>();
        sprite.color = noAuthority;
        rand = new System.Random(DateTime.Now.TimeOfDay.Milliseconds);
    }

    void OnEnable()
    {
        sprite.color = authority;
        Debug.Log("We received authority on: "+reader.Data.name);
        InvokeRepeating("DummyMovement", 1, 1);
    }
    void OnDisable()
    {
        sprite.color = noAuthority;
        Debug.Log("Lost authority");
        CancelInvoke();
    }

    void DummyMovement()
    {
        Point currentPoint = new Point(reader.Data.x, reader.Data.y);
        List<Point> possiblePoints = new List<Point>();
        foreach(var eachPoint in currentPoint.Ordinates())
            AddIfRelevant(eachPoint, possiblePoints);
        if (possiblePoints.Count == 0) return;
        int index = rand.Next(0, possiblePoints.Count);
        Point newPoint = possiblePoints[index];
        Debug.Log("Randomly decided to move from " + currentPoint + " to " + newPoint);
        
        SpatialOS.Commands.SendCommand(writer, Character.Commands.MoveTo.Descriptor, new MovementRequest(newPoint.x, newPoint.y), gameObject.EntityId(), op =>
        {
            if (op.StatusCode == Improbable.Worker.StatusCode.Success)
            {
                Debug.Log("Move response: " + (op.Response.HasValue ? op.Response.Value.allowed + "" : "NULL"));
            } else
            {
                Debug.Log("Move failed: " + op.ErrorMessage);
            }

        }, new TimeSpan(1000));
    }

    private void AddIfRelevant(Point point, List<Point> points)
    {
        if(GameWorld.instance.IsValidCoordinate(point) && GameWorld.instance.IsSquareFree(point)){
            points.Add(point);
        }
    }


}
