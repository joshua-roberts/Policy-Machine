{
  "nodes": [
    {
      "parentID": 0,
      "id": -9031252781558366036,
      "name": "super",
      "type": "U",
      "properties": {
        "password": "1004379edb46f1c23e812dfc94f7d43386cafaf512824779f090352cffc6dae9f420d93f6f3a347b7fa23d7118cded4ae7af783659fc7d284623523f4a64028024a597e3ec3ef24223e9d9edb3c80be99ba",
        "namespace": "super"
      }
    },
    {
      "parentID": 0,
      "id": -929075959112272980,
      "name": "super_ua1",
      "type": "UA",
      "properties": {
        "namespace": "super"
      }
    },
    {
      "parentID": 0,
      "id": 4933281359635583990,
      "name": "super",
      "type": "O",
      "properties": {
        "password": "1004379edb46f1c23e812dfc94f7d43386cafaf512824779f090352cffc6dae9f420d93f6f3a347b7fa23d7118cded4ae7af783659fc7d284623523f4a64028024a597e3ec3ef24223e9d9edb3c80be99ba",
        "namespace": "super"
      }
    },
    {
      "parentID": 0,
      "id": 8366115213598912729,
      "name": "super",
      "type": "OA",
      "properties": {
        "password": "1004379edb46f1c23e812dfc94f7d43386cafaf512824779f090352cffc6dae9f420d93f6f3a347b7fa23d7118cded4ae7af783659fc7d284623523f4a64028024a597e3ec3ef24223e9d9edb3c80be99ba",
        "namespace": "super"
      }
    },
    {
      "parentID": 0,
      "id": 6518102736717734688,
      "name": "super",
      "type": "PC",
      "properties": {
        "namespace": "super"
      }
    },
    {
      "parentID": 0,
      "id": 3423392340508832620,
      "name": "super_ua2",
      "type": "UA",
      "properties": {
        "namespace": "super"
      }
    }
  ],
  "assignments": [
    {
      "child": 3423392340508832620,
      "parent": 6518102736717734688
    },
    {
      "child": -9031252781558366036,
      "parent": 3423392340508832620
    },
    {
      "child": 8366115213598912729,
      "parent": 6518102736717734688
    },
    {
      "child": -9031252781558366036,
      "parent": -929075959112272980
    },
    {
      "child": 4933281359635583990,
      "parent": 8366115213598912729
    },
    {
      "child": -929075959112272980,
      "parent": 6518102736717734688
    }
  ],
  "associations": [
    {
      "ua": -929075959112272980,
      "target": 8366115213598912729,
      "ops": [
        "*"
      ]
    },
    {
      "ua": -929075959112272980,
      "target": 3423392340508832620,
      "ops": [
        "*"
      ]
    }
  ]
}