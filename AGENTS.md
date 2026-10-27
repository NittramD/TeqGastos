# TeqGastos agent instructions

## CodeGraph

This repository is indexed by CodeGraph (`.codegraph/` exists at the repository root).

- Use `codegraph_explore` before `grep`, `find`, broad file reads, or code edits whenever you need to locate or understand code.
- Pass the absolute project path `C:\Users\Martin\AndroidStudioProjects\TeqGastos` when the tool requires `projectPath`.
- Ask about concrete symbols, files, flows, callers, or blast radius. Treat source returned by CodeGraph as already read; do not reopen the same file unless additional context is genuinely missing.
- Use shell fallback `codegraph explore "<question or symbols>"` if the MCP tool is unavailable.
- After substantial source changes, confirm the index is current with `codegraph status`; refresh it with `codegraph index` only when needed.
- Do not commit generated CodeGraph database files. `.codegraph/.gitignore` keeps only the directory marker under version control.

## Engram persistent memory

Engram is the persistent-memory system for this project. Follow the globally installed Engram Memory Protocol and use the exposed `mem_*` tools.

- Before related work or any request to recall past work, query Engram (`mem_context`, then bounded `mem_search`, then `mem_get_observation` for selected results).
- Save only verified, durable information: architectural decisions, non-obvious discoveries, bug root causes/fixes, configuration changes, established patterns, and explicit user preferences.
- Use project scope for TeqGastos memories and stable `topic_key` values. Search the bounded topic before writing; update the existing observation instead of creating duplicates.
- Never store secrets, credentials, private data, speculation, raw logs, or routine progress.
- Do not invent an Engram session identity. Let the Codex integration bind the authoritative session ID.
- If Engram tools are unavailable, continue the task and report the integration issue instead of claiming that memory was saved.

