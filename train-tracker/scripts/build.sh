#!/bin/bash
set -e

echo "=== STARTING PRODUCTION BUILD FOR TRAIN COMPANION WORKSPACE ==="

# 1. Compile Shared Types
echo "Syncing Shared Data Transfer Objects..."
cd "$(dirname "$0")/../shared"
if [ -f "package.json" ]; then
  npm install
fi

# 2. Compile Backend Server
echo "Compiling Node.js + Express API Gateway..."
cd "../backend"
npm install
npm run build

# 3. Compile Admin Panel
echo "Bundling Vite + React + Tailwind Admin Dashboard..."
cd "../admin"
npm install
npm run build

echo "=== ALL MONOREPO SEGMENTS COMPILED SUCCESSFULLY ==="
