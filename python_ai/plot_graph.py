import matplotlib.pyplot as plt
from stable_baselines3.common import results_plotter
import os

# Calea către folderul unde se află monitor.csv
log_dir = "logs/tensorboard/"

print("[*] Citesc datele din monitor.csv...")

try:
    # Funcția magică din Stable Baselines care desenează graficul
    results_plotter.plot_results([log_dir], 500000, results_plotter.X_TIMESTEPS, "Evoluția Inteligenței Snake AI")

    # Salvăm imaginea în folderul principal
    plt.savefig("grafic_antrenament.png", bbox_inches='tight')
    print("[+] Succes! Imaginea a fost salvată ca 'grafic_antrenament.png'")

    # Afișăm fereastra cu graficul pe ecran
    plt.show()

except Exception as e:
    print(f"[-] Ceva nu a mers: {e}")