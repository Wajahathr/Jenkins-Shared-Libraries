def call(){
  // Hyphen hata dein warna agent par command not found ka error aayega
  sh "docker compose down && docker compose up -d"
}