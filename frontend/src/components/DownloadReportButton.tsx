export default function DownloadReportButton() {
  function handleDownload() {
    window.print();
  }

  return (
    <button
      type="button"
      onClick={handleDownload}
      title="Na janela de impressão, selecione Salvar como PDF."
      className="inline-flex items-center justify-center rounded-lg bg-teal-600 px-5 py-3 text-sm font-semibold text-white transition-colors hover:bg-teal-700 cursor-pointer"
    >
      Salvar dashboard em PDF
    </button>
  );
}
