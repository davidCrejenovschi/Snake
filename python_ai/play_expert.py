import socket
import json
import numpy as np
from stable_baselines3 import PPO

model = PPO.load("expert_snake")
server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
server.bind(('127.0.0.1', 65432))
server.listen(1)

print("[*] Serverul Expert este ONLINE.")

while True:
    conn, addr = server.accept()
    conn.settimeout(1.0) # <--- IMPORTANT: Dacă Java nu zice nimic 1 secundă, ieșim
    print(f"[+] Conexiune nouă: {addr}")

    try:
        while True:
            try:
                data = conn.recv(1024)
                if not data: break

                state = json.loads(data.decode('utf-8').strip())
                obs = np.array([state['head_x'], state['head_y'], state['food_x'], state['food_y']], dtype=np.float32)
                action, _ = model.predict(obs, deterministic=True)

                conn.sendall(f"{action}\n".encode('utf-8'))
            except socket.timeout:
                # Dacă a expirat timpul, verificăm dacă socket-ul e încă viu
                continue
    except Exception as e:
        print(f"[-] Final meci sau eroare: {e}")
    finally:
        conn.close()