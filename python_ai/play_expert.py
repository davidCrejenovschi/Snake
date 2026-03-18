import socket
import json
import numpy as np
from stable_baselines3 import PPO

# 1. Schimbăm numele modelului la cel nou, antrenat cu senzorii radar
model = PPO.load("expert_snake_radar")

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
server.bind(('127.0.0.1', 65432))
server.listen(1)

print("[*] Serverul Expert (RADAR) este ONLINE. Aștept conexiunea din Java...")

while True:
    conn, addr = server.accept()
    conn.settimeout(1.0)
    print(f"[+] Conexiune nouă: {addr}")

    try:
        while True:
            try:
                # 2. Am mărit bufferul la 2048 pentru că JSON-ul cu 24 de numere este mai lung
                data = conn.recv(2048)
                if not data: break

                state = json.loads(data.decode('utf-8').strip())

                # 3. Luăm direct lista "radar" din JSON și o transformăm în formatul pe care îl știe AI-ul
                obs = np.array(state['radar'], dtype=np.float32)

                # Prezicem următoarea mișcare
                action, _ = model.predict(obs, deterministic=True)

                conn.sendall(f"{action}\n".encode('utf-8'))
            except socket.timeout:
                continue
    except Exception as e:
        print(f"[-] Final meci sau eroare: {e}")
    finally:
        conn.close()