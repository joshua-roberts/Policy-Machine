{
   "nodes":
   [
      {
         "id":1,
         "name":"super",
         "type":"PC"
      },
      {
         "id":2,
         "name":"super",
         "type":"UA",
         "properties": 
            {
                "namespace": "super"
            }
         
      },
      {
         "id":3,
         "name":"super",
         "type":"U",
         "properties": 
            {
                "password": "super",
                "test": "test"
            }
      }
   ],
   "assignments":
   [
      {
         "child":2,
         "parent":1
      },
      {
         "child":3,
         "parent":2
      }
   ],
   "associations":[]
}