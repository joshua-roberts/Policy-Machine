{
   "nodes":[
      {
         "id":-1,
         "name":"Super PC",
         "type":"PC"
      },
      {
         "id":-2,
         "name":"super",
         "type":"UA",
         "properties": [
            {
                "key": "namespace",
                "value": "super"
            }
         ]
      },
      {
         "id":-3,
         "name":"super",
         "type":"U",
         "properties": [
            {
                "key": "password",
                "value": "super"
            }
         ]
      }
   ],
   "assignments":[
      {
         "child":-2,
         "parent":-1
      },
      {
         "child":-3,
         "parent":-2
      }
   ],
   "associations":[]
}