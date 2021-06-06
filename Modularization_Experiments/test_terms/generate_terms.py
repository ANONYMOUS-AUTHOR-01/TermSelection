import os

os.chdir("D:\Tasks\Modulirization\whole_test")
terms_sz = 28182
pers = [0.1,0.3,0.5,0.7]
oname = "foodon\\"
for per in pers:
    for fname in os.listdir(oname+"rank_random_iri\\"):
        lines = open(oname+"rank_random_iri\\"+fname).readlines()
        cur = lines
        with open("test_terms\\"+oname+"\\"+fname[:-4]+"_"+str(per)+".txt","w") as of:
            of.write(''.join(cur[:int(terms_sz*per)]))
    