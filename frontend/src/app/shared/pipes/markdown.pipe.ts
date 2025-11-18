import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Pipe({
  name: 'markdown',
  standalone: true
})
export class MarkdownPipe implements PipeTransform {
  
  constructor(private sanitizer: DomSanitizer) {}

  transform(text: string): SafeHtml {
    if (!text) return '';
    
    const formattedText = text
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/`([^`]+)`/g, '<code class="bg-base-200 text-brand-primary px-1 rounded">$1</code>')
      .split('\n')
      .map(line => line.trim().startsWith('* ') ? `<li>${line.substring(2)}</li>` : line)
      .join('\n')
      .replace(/<li>(.*?)<\/li>/g, '<ul><li>$1</li></ul>')
      .replace(/<\/ul>\n<ul>/g, '');

    return this.sanitizer.bypassSecurityTrustHtml(formattedText);
  }
}