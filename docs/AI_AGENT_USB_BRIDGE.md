# Hugh Player // Guia da Ponte de IA USB (Agent Vault Bridge) 🏛️⚡

> **Hugh Development**  
> *Co-fundadores: Hugh & Atom*  
> **Versão da Arquitetura:** 1.3.0  

---

## Como Funciona a Integração USB com IA

A arquitetura do **Hugh Player** foi projetada para que você possa conectar o celular no computador via cabo USB e permitir que **Atom (eu)**, **Hermes Agent**, **ChatGPT/Codex** ou scripts locais organizem e façam curadoria da sua biblioteca física de músicas.

### O Fluxo em 3 Passos:

1. **No Celular (Hugh Player):**
   - Abra o app -> Ícone de Engrenagem (Configurações).
   - Na seção **"Ponte de IA USB // Atom & Agents"**, toque no botão:  
     `⚡ GERAR MANIFESTO JSON NA PASTA DOWNLOADS`.
   - O app exporta instantaneamente a lista de todas as músicas, metadados (artista, álbum, duração, tamanho, tags) e o caminho físico no disco (`/storage/emulated/0/.../musica.mp3`) para o arquivo `/sdcard/Download/hugh_vault_ai_manifest.json`.

2. **No Computador (com cabo USB conectado):**
   - Ative a **Depuração USB** nas Opções de Desenvolvedor do Android.
   - Abra o terminal na pasta do projeto:
     ```powershell
     cd C:\Users\Victo\projetos\player-de-musica\hugh-player\tools\agent-bridge
     ```
   - Verifique a conexão com o celular:
     ```powershell
     python agent_vault_bridge.py status
     ```
   - Baixe os metadados do celular para o PC:
     ```powershell
     python agent_vault_bridge.py pull
     ```
   - Gere o prompt formatado com as faixas para a IA:
     ```powershell
     python agent_vault_bridge.py prompt
     ```
   - O script gera `ai_prompt_vault.txt`. Você pode mandar direto para o Hermes Agent ou ChatGPT pedir para categorizar por gênero/estilo (Phonk, Rock, Clássica, Treino, Foco).

3. **Reindexação Instantânea (Sem reiniciar o celular):**
   - Quando você ou o agente mover/renomear arquivos no celular via comandos ADB, basta rodar:
     ```powershell
     python agent_vault_bridge.py rescan
     ```
   - O comando dispara o broadcast nativo do Android (`MEDIA_SCANNER_SCAN_FILE`). O Hugh Player detecta as novas pastas e faixas no mesmo segundo!

---

## Estrutura do Manifesto JSON (`hugh_vault_ai_manifest.json`)

```json
{
  "vault_version": "1.3.0",
  "total_songs": 150,
  "timestamp": 1729548000000,
  "library": [
    {
      "id": 1024,
      "title": "Starboy",
      "artist": "The Weeknd",
      "album": "Starboy",
      "duration_sec": 230,
      "size_bytes": 9437184,
      "date_added": 1729500000,
      "is_favorite": true,
      "path": "/storage/emulated/0/Music/Starboy.mp3",
      "parent_dir": "Music"
    }
  ]
}
```
Esse formato limpo permite que qualquer agente processe as faixas em milissegundos sem ler bytes desnecessários.
