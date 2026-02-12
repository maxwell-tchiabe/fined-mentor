import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Pipe({
  name: 'markdown',
  standalone: true
})
export class MarkdownPipe implements PipeTransform {

  constructor(private sanitizer: DomSanitizer) { }

  transform(text: string): SafeHtml {
    if (!text) return '';

    let html = text;

    // Code blocks (```code```)
    html = html.replace(/```([\s\S]*?)```/g, '<pre class="bg-base-200 p-3 rounded my-2 overflow-x-auto"><code>$1</code></pre>');

    // Horizontal rules (---)
    html = html.replace(/^---$/gm, '<hr class="my-4 border-base-300" />');

    // Headers (h1-h6)
    html = html.replace(/^######\s+(.+)$/gm, '<h6 class="text-base font-semibold mt-4 mb-2">$1</h6>');
    html = html.replace(/^#####\s+(.+)$/gm, '<h5 class="text-lg font-semibold mt-4 mb-2">$1</h5>');
    html = html.replace(/^####\s+(.+)$/gm, '<h4 class="text-xl font-semibold mt-4 mb-2 text-brand-500">$1</h4>');
    html = html.replace(/^###\s+(.+)$/gm, '<h3 class="text-2xl font-bold mt-5 mb-3 text-brand-500">$1</h3>');
    html = html.replace(/^##\s+(.+)$/gm, '<h2 class="text-3xl font-bold mt-6 mb-3">$1</h2>');
    html = html.replace(/^#\s+(.+)$/gm, '<h1 class="text-4xl font-bold mt-6 mb-4">$1</h1>');

    // Bold text (**text**)
    html = html.replace(/\*\*(.*?)\*\*/g, '<strong class="font-semibold">$1</strong>');

    // Italic text (*text* or _text_)
    html = html.replace(/\*([^\*\n]+)\*/g, '<em class="italic">$1</em>');
    html = html.replace(/_([^_\n]+)_/g, '<em class="italic">$1</em>');

    // Inline code (`code`)
    html = html.replace(/`([^`]+)`/g, '<code class="bg-base-200 text-brand-500 px-1.5 py-0.5 rounded text-sm">$1</code>');

    // Links ([text](url))
    html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g,
      '<a href="$2" class="text-brand-500 font-medium underline decoration-brand-500/30 underline-offset-4 hover:decoration-brand-500 hover:text-brand-600 transition-all inline-flex items-center gap-1" target="_blank" rel="noopener noreferrer">' +
      '$1' +
      '<svg class="w-3 h-3 opacity-60" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" /></svg>' +
      '</a>');

    // Process lists - we need to handle this line by line
    const lines = html.split('\n');
    const processedLines: string[] = [];
    let inUnorderedList = false;
    let inOrderedList = false;
    let listDepth = 0;

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i];
      const trimmedLine = line.trim();

      // Detect list item depth by counting leading spaces/tabs
      const leadingSpaces = line.match(/^(\s*)/)?.[1].length || 0;
      const currentDepth = Math.floor(leadingSpaces / 2);

      // Unordered list item (*, -, or +)
      if (/^[\*\-\+]\s+/.test(trimmedLine)) {
        const content = trimmedLine.substring(2);

        if (!inUnorderedList || currentDepth > listDepth) {
          processedLines.push('<ul class="list-disc list-inside ml-4 my-2 space-y-1">');
          inUnorderedList = true;
          inOrderedList = false;
        }

        processedLines.push(`<li class="ml-${currentDepth * 4}">${content}</li>`);
        listDepth = currentDepth;
      }
      // Ordered list item (1., 2., etc.)
      else if (/^\d+\.\s+/.test(trimmedLine)) {
        const content = trimmedLine.replace(/^\d+\.\s+/, '');

        if (!inOrderedList || currentDepth > listDepth) {
          processedLines.push('<ol class="list-decimal list-inside ml-4 my-2 space-y-1">');
          inOrderedList = true;
          inUnorderedList = false;
        }

        processedLines.push(`<li class="ml-${currentDepth * 4}">${content}</li>`);
        listDepth = currentDepth;
      }
      // Not a list item
      else {
        // Close any open lists
        if (inUnorderedList) {
          processedLines.push('</ul>');
          inUnorderedList = false;
        }
        if (inOrderedList) {
          processedLines.push('</ol>');
          inOrderedList = false;
        }
        listDepth = 0;

        // Add the line (could be paragraph, heading, etc.)
        if (trimmedLine) {
          // If it's not already HTML (doesn't start with <), wrap in paragraph
          if (!trimmedLine.startsWith('<')) {
            processedLines.push(`<p class="my-2">${line}</p>`);
          } else {
            processedLines.push(line);
          }
        } else {
          // Empty line - add some spacing
          processedLines.push('<br />');
        }
      }
    }

    // Close any remaining open lists
    if (inUnorderedList) {
      processedLines.push('</ul>');
    }
    if (inOrderedList) {
      processedLines.push('</ol>');
    }

    html = processedLines.join('\n');

    // Clean up excessive line breaks
    html = html.replace(/(<br\s*\/?>[\s\n]*){3,}/g, '<br /><br />');

    return this.sanitizer.bypassSecurityTrustHtml(html);
  }
}