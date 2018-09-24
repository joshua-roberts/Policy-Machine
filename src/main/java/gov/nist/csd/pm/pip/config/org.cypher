match(n) detach delete n;

USING PERIODIC COMMIT 10000 LOAD CSV WITH HEADERS FROM "file:///test.csv" AS row create(:T{id:TOFLOAT(row.id), name:row.name, type:row.type, org_type:row.org_type});
match(n:T) where n.type='OA' set n:OA;
match(n:T) where n.type='UA' set n:UA;
match(n:T) where n.type='PC' set n:PC;

match(n) where n.org_type='ORG' set n:ORG;
match(n) where n.org_type='FOLDER' set n:FOLDER;
match(n) where n.org_type='PROJECT' set n:PROJECT;
match(n) where n.org_type='PR' set n:PR;

create constraint on (n:node) assert n.id is UNIQUE;

match(x:ORG),(y:PC{id: 999999990}) create (x)-[:assigned_to]->(y);

match(x:FOLDER:OA), (y:ORG:OA) where x.id % 100 = y.id % 100 create (x)-[:assigned_to]->(y);
match(x:FOLDER:UA), (y:ORG:UA) where x.id % 100 = y.id % 100 create (x)-[:assigned_to]->(y);

match(x:PROJECT:OA), (y:FOLDER:OA) where x.id % 100 = y.id % 100 create (x)-[:assigned_to]->(y);
match(x:PROJECT:UA), (y:FOLDER:UA) where x.id % 100 = y.id % 100 create (x)-[:assigned_to]->(y);

match(x:PR:OA), (y:PROJECT:OA) where x.id % 100 = y.id % 100 create (x)-[:assigned_to]->(y);
match(x:PR:UA), (y:PROJECT:UA) where x.id % 100 = y.id % 100 create (x)-[:assigned_to]->(y);

match(x:OA:ORG), (y:UA:ORG) where x.id % 100 = y.id % 100 create (y)-[:association{operations:['file read']}]->(x);
match(x:OA:FOLDER), (y:UA:FOLDER) where x.id % 100000 = y.id % 100000 create (y)-[:association{operations:['file read']}]->(x);
match(x:OA:PROJECT), (y:UA:PROJECT) where x.id % 100000000 = y.id % 100000000 create (y)-[:association{operations:['file read']}]->(x);