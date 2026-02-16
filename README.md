# AnunciosLocBackend 🚀
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://example.com) [![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://example.com)

## 📚 Table of Contents
- [Description](#description)
- [Installation](#installation)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Curl Examples](#curl-examples)
- [Testing](#testing)
- [Contributing](#contributing)
- [Troubleshooting](#troubleshooting)
- [Authors](#authors)

## ✨ Description
AnunciosLocBackend is a robust backend application for managing advertisements with features like user authentication, ad posting, and search.

## ⚙️ Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/XavierDelacth/AnunciosLocBackend.git
   cd AnunciosLocBackend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```

## ⚙️ Configuration
- Update the `.env` file with your database and API keys.

## 📡 API Documentation
### Endpoints
- **POST /api/ads**: Create a new advertisement
- **GET /api/ads**: Retrieve all advertisements
- **GET /api/ads/:id**: Retrieve an advertisement by ID

## 💻 Curl Examples
```bash
# Create an advertisement
curl -X POST -H "Content-Type: application/json" -d '{"title":"My Ad","description":"Ad details"}' http://localhost:3000/api/ads

# Retrieve all advertisements
curl -X GET http://localhost:3000/api/ads
```

## ✅ Testing
Run the tests using:
```bash
npm test
```

## 🤝 Contributing
We welcome contributions! Please read our [contribution guidelines](CONTRIBUTING.md) for details.

## 🐛 Troubleshooting
| Issue                   | Solution                             |
|-------------------------|-------------------------------------|
| Unable to connect to DB | Check your database connection URL. |
| Missing package error   | Run `npm install` to install missing packages. |

## 👤 Authors
| Name           | Role         |
|----------------|--------------|
| Xavier Delacth | Maintainer   |
| Other Contributor | Contributor | 
