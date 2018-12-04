{
    "nodes":[
        {
            "name": "pc1",
            "type": "PC"
        },
        {
            "name": "oa1",
            "type": "OA"
        },
        {
            "name": "o1",
            "type": "O"
        },
        {
            "name": "ua1",
            "type": "UA"
        },
        {
            "name": "u1",
            "type": "U"
        }
    ],
    "assignments":[
        {
            "child": ":u1",
            "parent": ":ua1"
        },
        {
            "child": ":ua1",
            "parent": ":pc1"
        },
        {
            "child": ":o1",
            "parent": ":oa1"
        },
        {
            "child": ":oa1",
            "parent": ":pc1"
        },

    ],
    "associations":[
        {
            "source": ":ua1",
            "target": ":oa1",
            "operations":["r", "w"]
        }
    ]
}