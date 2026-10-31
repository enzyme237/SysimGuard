
import os, sys, subprocess
import pandas as pd
from androguard.misc import AnalyzeAPK

CLE = os.environ["ANDROZOO_KEY"]
liste = pd.read_csv("a_telecharger.csv")

with open("permissions.txt") as f:
    attendues = [l.strip() for l in f if l.strip()]

resultats = []
echecs = 0
for i, row in liste.iterrows():
    sha = row["sha256"]
    apk = f"{sha}.apk"
    url = f"https://androzoo.uni.lu/api/download?apikey={CLE}&sha256={sha}"
    try:
        subprocess.run(["curl", "-s", "-o", apk, url], check=True, timeout=120)
        a, _, _ = AnalyzeAPK(apk)
        perms = set(p.split(".")[-1] for p in a.get_permissions())
        ligne = {perm: (1 if perm in perms else 0) for perm in attendues}
        ligne["sha256"] = sha
        ligne["annee"] = row["annee"]
        ligne["Malware"] = 0
        resultats.append(ligne)
    except Exception as e:
        echecs += 1
        print(f"[{i}] echec {sha[:12]} : {e}", flush=True)
    finally:
        if os.path.exists(apk):
            os.remove(apk)
    if (i + 1) % 50 == 0:
        pd.DataFrame(resultats).to_csv("sains_recents_permissions.csv", index=False)
        print(f"{i+1}/{len(liste)} traites, {echecs} echecs", flush=True)

pd.DataFrame(resultats).to_csv("sains_recents_permissions.csv", index=False)
print(f"TERMINE : {len(resultats)} applications, {echecs} echecs")
