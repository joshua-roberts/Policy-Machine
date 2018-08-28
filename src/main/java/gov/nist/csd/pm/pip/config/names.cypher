match(n) where n.id > 100000010 detach delete n;
match(n{name:'names OA'}) detach delete n;
match(n{name:'names PC'}) detach delete n;
USING PERIODIC COMMIT 10000 LOAD CSV WITH HEADERS FROM "file:///names.csv" AS row create(n:OA:ROW{name:row.Number, type:'OA', id:TOINT(row.Number), Number:TOINT(row.Number),
                                                                                                  GivenName:row.GivenName,
                                                                                                  NameSet:row.NameSet,
                                                                                                  MiddleInitial:row.MiddleInitial,
                                                                                                  Title:row.Title,
                                                                                                  Gender:row.Gender,
                                                                                                  Surname:row.Surname,
                                                                                                  StreetAddress:row.StreetAddress,
                                                                                                  City:row.City,
                                                                                                  ZipCode:row.ZipCode,
                                                                                                  EmailAddress:row.EmailAddress,
                                                                                                  TelephoneNumber:row.TelephoneNumber,
                                                                                                  Age:row.Age,
                                                                                                  Occupation:row.Occupation});

create (n:PC{id: 9999999999, name:'names PC', type: 'PC'});
create (n:OA{id: 9999999998, name:'names OA', type: 'OA'});
match(n:PC{name:'names PC'}), (m:OA{name:'names OA'}) create (m)-[:assigned_to]->(n);

create (n:UA{id: 9999999997, name:'names PC admin', type: 'UA'});
match(n:PC{name:'names PC'}), (m:OA{name:'names PC admin'}) create (m)-[:assigned_to]->(n);

match(n:U{name:'super'}), (m:UA{name:'names PC admin'}) create (m)-[:assigned_to]->(n);
match(n:OA{name:'names OA'}), (m:UA{name:'names PC admin'}) create (m)-[:association{operations:['*'], inherit:true}]->(n);



create (n:PC{id: 9999999996, name:'names RBAC', type: 'PC'});
create (n:OA{id: 9999999995, name:'names RBAC', type: 'OA'});
match(n:PC{name:'names RBAC'}), (m:OA{name:'names RBAC'}) create (m)-[:assigned_to]->(n);

create (n:UA{id: 9999999994, name:'names RBAC admin', type: 'UA'});
match(n:PC{name:'names RBAC'}), (m:OA{name:'names RBAC admin'}) create (m)-[:assigned_to]->(n);

match(n:U{name:'super'}), (m:UA{name:'names RBAC admin'}) create (m)-[:assigned_to]->(n);
match(n:OA{name:'names RBAC'}), (m:UA{name:'names RBAC admin'}) create (m)-[:association{operations:['*'], inherit:true}]->(n);



match(n:OA:ROW) create (o:O:CELL{name:n.GivenName, type:'O', id:TOINT(n.Number+'1')});
match(n:OA:ROW) create (o:O:CELL{name:n.NameSet, type:'O', id:TOINT(n.Number+'2')});
match(n:OA:ROW) create (o:O:CELL{name:n.MiddleInitial, type:'O', id:TOINT(n.Number+'3')});
match(n:OA:ROW) create (o:O:CELL{name:n.Title, type:'O', id:TOINT(n.Number+'4')});
match(n:OA:ROW) create (o:O:CELL{name:n.Gender, type:'O', id:TOINT(n.Number+'5')});
match(n:OA:ROW) create (o:O:CELL{name:n.Surname, type:'O', id:TOINT(n.Number+'6')});
match(n:OA:ROW) create (o:O:CELL{name:n.StreetAddress, type:'O', id:TOINT(n.Number+'7')});
match(n:OA:ROW) create (o:O:CELL{name:n.City, type:'O', id:TOINT(n.Number+'8')});
match(n:OA:ROW) create (o:O:CELL{name:n.ZipCode, type:'O', id:TOINT(n.Number+'9')});
match(n:OA:ROW) create (o:O:CELL{name:n.EmailAddress, type:'O', id:TOINT(n.Number+'10')});
match(n:OA:ROW) create (o:O:CELL{name:n.TelephoneNumber, type:'O', id:TOINT(n.Number+'11')});
match(n:OA:ROW) create (o:O:CELL{name:n.Age, type:'O', id:TOINT(n.Number+'12')});
match(n:OA:ROW) create (o:O:CELL{name:n.Occupation, type:'O', id:TOINT(n.Number+'13')});

match(n:OA:ROW), (o:O:CELL{name:n.GivenName, type:'O', id:TOINT(n.Number+'1')}) create (o)-[:assigned_to]->(n);
match(n:OA:ROW), (o:O:CELL{name:n.GivenName, type:'O', id:TOINT(n.Number+'2')}) create (o)-[:assigned_to]->(n);
match(n:OA:ROW), (o:O:CELL{name:n.GivenName, type:'O', id:TOINT(n.Number+'3')}) create (o)-[:assigned_to]->(n);
match(n:OA:ROW), (o:O:CELL{name:n.GivenName, type:'O', id:TOINT(n.Number+'4')}) create (o)-[:assigned_to]->(n);
match(n:OA:ROW), (o:O:CELL{name:n.GivenName, type:'O', id:TOINT(n.Number+'5')}) create (o)-[:assigned_to]->(n);
match(n:OA:ROW), (o:O:CELL{name:n.GivenName, type:'O', id:TOINT(n.Number+'6')}) create (o)-[:assigned_to]->(n);
match(n:OA:ROW), (o:O:CELL{name:n.GivenName, type:'O', id:TOINT(n.Number+'7')}) create (o)-[:assigned_to]->(n);
match(n:OA:ROW), (o:O:CELL{name:n.GivenName, type:'O', id:TOINT(n.Number+'8')}) create (o)-[:assigned_to]->(n);
match(n:OA:ROW), (o:O:CELL{name:n.GivenName, type:'O', id:TOINT(n.Number+'9')}) create (o)-[:assigned_to]->(n);
match(n:OA:ROW), (o:O:CELL{name:n.GivenName, type:'O', id:TOINT(n.Number+'10')}) create (o)-[:assigned_to]->(n);
match(n:OA:ROW), (o:O:CELL{name:n.GivenName, type:'O', id:TOINT(n.Number+'11')}) create (o)-[:assigned_to]->(n);
match(n:OA:ROW), (o:O:CELL{name:n.GivenName, type:'O', id:TOINT(n.Number+'12')}) create (o)-[:assigned_to]->(n);
match(n:OA:ROW), (o:O:CELL{name:n.GivenName, type:'O', id:TOINT(n.Number+'13')}) create (o)-[:assigned_to]->(n);

match(n:OA{name:'names OA'}), (m:ROW) create (m)-[:assigned_to]->(n);

match(n:OA{id: 9999999995}) create(OA{id: 9999999994, name: 'group 1 names', type: 'OA'})-[:assigned_to]->(n);
match(n:OA{id: 9999999995}) create(OA{id: 9999999993, name: 'group 2 names', type: 'OA'})-[:assigned_to]->(n);
match(n:OA{id: 9999999995}) create(OA{id: 9999999992, name: 'group 3 names', type: 'OA'})-[:assigned_to]->(n);
match(n:OA{id: 9999999995}) create(OA{id: 9999999991, name: 'group 4 names', type: 'OA'})-[:assigned_to]->(n);
match(n:OA{id: 9999999995}) create(OA{id: 9999999990, name: 'group 5 names', type: 'OA'})-[:assigned_to]->(n);
match(n:OA{id: 9999999995}) create(OA{id: 9999999989, name: 'group 6 names', type: 'OA'})-[:assigned_to]->(n);
match(n:OA{id: 9999999995}) create(OA{id: 9999999988, name: 'group 7 names', type: 'OA'})-[:assigned_to]->(n);
match(n:OA{id: 9999999995}) create(OA{id: 9999999987, name: 'group 8 names', type: 'OA'})-[:assigned_to]->(n);
match(n:OA{id: 9999999995}) create(OA{id: 9999999986, name: 'group 9 names', type: 'OA'})-[:assigned_to]->(n);
match(n:OA{id: 9999999995}) create(OA{id: 9999999985, name: 'group 10 names', type: 'OA'})-[:assigned_to]->(n);

match(n{name:'group 1 names'}), (m:ROW) where m.id % 10 = 1 create (m)-[:assigned_to]->(n);
match(n{name:'group 2 names'}), (m:ROW) where m.id % 10 = 2 create (m)-[:assigned_to]->(n);
match(n{name:'group 3 names'}), (m:ROW) where m.id % 10 = 3 create (m)-[:assigned_to]->(n);
match(n{name:'group 4 names'}), (m:ROW) where m.id % 10 = 4 create (m)-[:assigned_to]->(n);
match(n{name:'group 5 names'}), (m:ROW) where m.id % 10 = 5 create (m)-[:assigned_to]->(n);
match(n{name:'group 6 names'}), (m:ROW) where m.id % 10 = 6 create (m)-[:assigned_to]->(n);
match(n{name:'group 7 names'}), (m:ROW) where m.id % 10 = 7 create (m)-[:assigned_to]->(n);
match(n{name:'group 8 names'}), (m:ROW) where m.id % 10 = 8 create (m)-[:assigned_to]->(n);
match(n{name:'group 9 names'}), (m:ROW) where m.id % 10 = 9 create (m)-[:assigned_to]->(n);
match(n{name:'group 10 names'}), (m:ROW) where m.id % 10 = 0 create (m)-[:assigned_to]->(n);


match(n:PC{id: 9999999996}) create(:UA{id: 9999999984, name: 'group 1 names', type: 'UA'})-[:assigned_to]->(n);
match(n:PC{id: 9999999996}) create(:UA{id: 9999999983, name: 'group 2 names', type: 'UA'})-[:assigned_to]->(n);
match(n:PC{id: 9999999996}) create(:UA{id: 9999999982, name: 'group 3 names', type: 'UA'})-[:assigned_to]->(n);
match(n:PC{id: 9999999996}) create(:UA{id: 9999999981, name: 'group 4 names', type: 'UA'})-[:assigned_to]->(n);
match(n:PC{id: 9999999996}) create(:UA{id: 9999999980, name: 'group 5 names', type: 'UA'})-[:assigned_to]->(n);
match(n:PC{id: 9999999996}) create(:UA{id: 9999999979, name: 'group 6 names', type: 'UA'})-[:assigned_to]->(n);
match(n:PC{id: 9999999996}) create(:UA{id: 9999999978, name: 'group 7 names', type: 'UA'})-[:assigned_to]->(n);
match(n:PC{id: 9999999996}) create(:UA{id: 9999999977, name: 'group 8 names', type: 'UA'})-[:assigned_to]->(n);
match(n:PC{id: 9999999996}) create(:UA{id: 9999999976, name: 'group 9 names', type: 'UA'})-[:assigned_to]->(n);
match(n:PC{id: 9999999996}) create(:UA{id: 9999999975, name: 'group 10 names', type: 'UA'})-[:assigned_to]->(n);

match(n:OA{id: 9999999994}), (m:UA{id: 9999999984}) create(m)-[:association{operations:['*'], inherit:true}]->(n);
match(n:OA{id: 9999999993}), (m:UA{id: 9999999983}) create(m)-[:association{operations:['*'], inherit:true}]->(n);
match(n:OA{id: 9999999992}), (m:UA{id: 9999999982}) create(m)-[:association{operations:['*'], inherit:true}]->(n);
match(n:OA{id: 9999999991}), (m:UA{id: 9999999981}) create(m)-[:association{operations:['*'], inherit:true}]->(n);
match(n:OA{id: 9999999990}), (m:UA{id: 9999999980}) create(m)-[:association{operations:['*'], inherit:true}]->(n);
match(n:OA{id: 9999999989}), (m:UA{id: 9999999979}) create(m)-[:association{operations:['*'], inherit:true}]->(n);
match(n:OA{id: 9999999988}), (m:UA{id: 9999999978}) create(m)-[:association{operations:['*'], inherit:true}]->(n);
match(n:OA{id: 9999999987}), (m:UA{id: 9999999977}) create(m)-[:association{operations:['*'], inherit:true}]->(n);
match(n:OA{id: 9999999986}), (m:UA{id: 9999999976}) create(m)-[:association{operations:['*'], inherit:true}]->(n);
match(n:OA{id: 9999999985}), (m:UA{id: 9999999975}) create(m)-[:association{operations:['*'], inherit:true}]->(n);

