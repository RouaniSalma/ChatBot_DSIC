import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
};
module.exports = {
  images: {
    domains: ['localhost'], // Autorise les images depuis localhost
    path: '/api/images',   // Préfixe pour les requêtes d'images
  },
}
export default nextConfig;
