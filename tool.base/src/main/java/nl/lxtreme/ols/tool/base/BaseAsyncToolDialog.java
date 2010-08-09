/*
 * OpenBench LogicSniffer / SUMP project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 *
 * Copyright (C) 2006-2010 Michael Poppitz, www.sump.org
 * Copyright (C) 2010 J.W. Janssen, www.lxtreme.nl
 */
package nl.lxtreme.ols.tool.base;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.logging.*;

import javax.swing.*;

import nl.lxtreme.ols.api.*;
import nl.lxtreme.ols.util.swing.*;


/**
 * @author jawi
 */
public abstract class BaseAsyncToolDialog<RESULT_TYPE, WORKER extends BaseAsyncToolWorker<RESULT_TYPE>> extends
BaseToolDialog implements AsyncToolDialog<RESULT_TYPE, WORKER>, ExportAware<RESULT_TYPE>, Configurable
{
  // INNER TYPES

  /**
   * 
   */
  protected final class ExportAction extends AbstractAction
  {
    // CONSTANTS

    private static final long serialVersionUID = 1L;

    // CONSTRUCTORS

    /**
     * 
     */
    public ExportAction()
    {
      super( "Export" );
      putValue( SHORT_DESCRIPTION, "Exports the analysis results to file" );
    }

    // METHODS

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed( final ActionEvent aEvent )
    {
      final File selectedFile = SwingComponentUtils.showFileSaveDialog( getOwner(), StdFileFilter.CSV,
          StdFileFilter.HTML );

      final RESULT_TYPE analysisResult = getAnalysisResult();
      if ( selectedFile != null )
      {
        if ( LOG.isLoggable( Level.INFO ) )
        {
          LOG.info( "Writing analysis results to " + selectedFile.getPath() );
        }

        final String filename = selectedFile.getName();
        if ( filename.endsWith( ".htm" ) || filename.endsWith( ".html" ) )
        {
          storeToHtmlFile( selectedFile, analysisResult );
        }
        else
        {
          storeToCsvFile( selectedFile, analysisResult );
        }
      }
    }
  }

  /**
   * 
   */
  protected final class RunAnalysisAction extends AbstractAction
  {
    // CONSTANTS

    private static final long serialVersionUID = 1L;

    // CONSTRUCTORS

    /**
     * 
     */
    public RunAnalysisAction()
    {
      super( "Analyze" );
      restore();
    }

    // METHODS

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed( final ActionEvent aEvent )
    {
      final String name = ( String )getValue( NAME );

      if ( "Abort".equals( name ) )
      {
        cancelToolWorker();

        putValue( NAME, "Analyze" );
      }
      else
      {
        startToolWorker();

        putValue( NAME, "Abort" );
        putValue( SHORT_DESCRIPTION, "Aborts current analysis..." );
      }
    }

    /**
     * 
     */
    public void restore()
    {
      putValue( NAME, "Analyze" );
      putValue( SHORT_DESCRIPTION, "Run analysis" );
    }
  }

  // CONSTANTS

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger( BaseAsyncToolDialog.class.getName() );

  // VARIABLES

  private transient volatile WORKER toolWorker;
  private transient volatile RESULT_TYPE analysisResult;

  // CONSTRUCTORS

  /**
   * @param aOwner
   * @param aName
   */
  protected BaseAsyncToolDialog( final Window aOwner, final String aName )
  {
    super( aOwner, aName );
  }

  // METHODS

  /**
   * @see nl.lxtreme.ols.tool.base.ExportAware#createReport(java.lang.Object)
   */
  public void createReport( final RESULT_TYPE aAnalysisResult )
  {
    this.analysisResult = aAnalysisResult;

    setControlsEnabled( true );
  }

  /**
   * @see nl.lxtreme.ols.tool.base.AsyncToolDialog#setToolWorker(nl.lxtreme.ols.tool.base.BaseAsyncToolWorker)
   */
  public void setToolWorker( final WORKER aToolWorker )
  {
    this.toolWorker = aToolWorker;
  }

  /**
   * Cancels all running tool workers.
   */
  final void cancelToolWorker()
  {
    synchronized ( this.toolWorker )
    {
      this.analysisResult = null;
      this.toolWorker.cancel( true /* mayInterruptIfRunning */);
      setControlsEnabled( true );
    }
  }

  /**
   * Starts the tool worker.
   */
  final void startToolWorker()
  {
    synchronized ( this.toolWorker )
    {
      setupToolWorker( this.toolWorker );

      this.toolWorker.execute();

      setControlsEnabled( false );
    }
  }

  /**
   * Closes this dialog, cancels any running tool workers if they are still running.
   */
  @Override
  protected void close()
  {
    synchronized ( this.toolWorker )
    {
      cancelToolWorker();
      super.close();
    }
  }

  /**
   * @return
   */
  protected final RESULT_TYPE getAnalysisResult()
  {
    return this.analysisResult;
  }

  /**
   * @param aB
   */
  protected abstract void setControlsEnabled( final boolean aEnabled );

  /**
   * @param aToolWorker
   */
  protected abstract void setupToolWorker( final WORKER aToolWorker );

  /**
   * @param aSelectedFile
   * @param aAnalysisResult
   */
  protected abstract void storeToCsvFile( final File aSelectedFile, final RESULT_TYPE aAnalysisResult );

  /**
   * @param aSelectedFile
   * @param aAnalysisResult
   */
  protected abstract void storeToHtmlFile( final File aSelectedFile, final RESULT_TYPE aAnalysisResult );

}