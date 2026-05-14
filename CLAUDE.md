# Boatsharing App

## Project Structure
- `/backend` — Java 17, Spring Boot, Spring Boot Testcontainer, Spring Security, JPA, PostgreSQL
- `/frontend` — Angular 19, TypeScript strict mode

## General Rules
- Never modify both frontend and backend in the same task
- Never hardcode secrets, API keys, or credentials
- Never edit `.env` files, only `.env.example`
- Always follow existing naming conventions in the file you're editing
- Always create or update tests when changing logic
- Commit-ready code only — no TODOs left in implementation

## Git
- Branch naming: `feature/`, `fix/`, `refactor/`
- Commit messages: imperative mood, max 72 chars
- Never force push, never commit to main directly

## What NOT to touch
- `.env`
- `**/secrets/**`
- `~/.ssh`
- Any migration file that has already been run